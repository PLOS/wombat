/*
 * Copyright (c) 2017 Public Library of Science
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

package org.ambraproject.wombat.controller;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteStreams;
import com.google.gson.Gson;
import org.ambraproject.wombat.config.RuntimeConfiguration;
import org.ambraproject.wombat.config.site.RequestMappingContextDictionary;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteParam;
import org.ambraproject.wombat.config.site.SiteSet;
import org.ambraproject.wombat.config.site.url.Link;
import org.ambraproject.wombat.identity.ArticlePointer;
import org.ambraproject.wombat.identity.RequestedDoiVersion;
import org.ambraproject.wombat.model.EmailMessage;
import org.ambraproject.wombat.model.Reference;
import org.ambraproject.wombat.service.*;
import org.ambraproject.wombat.service.remote.CachedRemoteService;
import org.ambraproject.wombat.service.remote.CorpusContentApi;
import org.ambraproject.wombat.service.remote.JsonService;
import org.ambraproject.wombat.service.remote.ServiceRequestException;
import org.ambraproject.wombat.service.remote.orcid.OrcidApi;
import org.ambraproject.wombat.service.remote.orcid.OrcidAuthenticationTokenExpiredException;
import org.ambraproject.wombat.service.remote.orcid.OrcidAuthenticationTokenReusedException;
import org.apache.commons.io.output.WriterOutputStream;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.apache.commons.validator.routines.UrlValidator;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;
import org.w3c.dom.Document;

import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.InternetAddress;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller for rendering an article.
 */
@Controller
public class ArticleController extends WombatController {

  private static final Logger log = LoggerFactory.getLogger(ArticleController.class);

  /**
   * Initial size (in bytes) of buffer that holds transformed article HTML before passing it to the model.
   */
  private static final int XFORM_BUFFER_SIZE = 0x8000;
  private static final int MAX_TO_EMAILS = 5;

  @Autowired
  private Charset charset;
  @Autowired
  private CorpusContentApi corpusContentApi;
  @Autowired
  private ArticleTransformService articleTransformService;
  @Autowired
  private CachedRemoteService<Reader> cachedRemoteReader;
  @Autowired
  private JsonService jsonService;
  @Autowired
  private HoneypotService honeypotService;
  @Autowired
  private FreeMarkerConfig freeMarkerConfig;
  @Autowired
  private FreemarkerMailService freemarkerMailService;
  @Autowired
  private JavaMailSender javaMailSender;
  @Autowired
  private ArticleMetadata.Factory articleMetadataFactory;
  @Autowired
  private ParseXmlService parseXmlService;
  @Autowired
  private Gson gson;
  @Autowired
  private RuntimeConfiguration runtimeConfiguration;
  @Autowired
  private SiteSet siteSet;
  @Autowired
  private RequestMappingContextDictionary requestMappingContextDictionary;
  @Autowired
  private DoiToJournalResolutionService doiToJournalResolutionService;
  @Autowired
  private OrcidApi orcidApi;

  // TODO: this method currently makes 5 backend RPCs, all sequentially. Explore reducing this
  // number, or doing them in parallel, if this is a performance bottleneck.
  @RequestMapping(name = "article", value = "/article")
  public String renderArticle(HttpServletRequest request,
                              Model model,
                              @SiteParam Site site,
                              RequestedDoiVersion articleId)
      throws IOException {
    ArticlePointer articlePointer = articleMetadataFactory.get(site, articleId)
        .validateVisibility("article")
        .populate(request, model)
        .fillAmendments(model)
        .getArticlePointer();

    XmlContent xmlContent = getXmlContent(site, articlePointer, request);
    model.addAttribute("articleText", xmlContent.html);
    model.addAttribute("references", xmlContent.references);

    return site + "/ftl/article/article";
  }

  /**
   * Serves a request for the "about the authors" page for an article.
   *
   * @param model     data to pass to the view
   * @param site      current site
   * @param articleId specifies the article
   * @return path to the template
   * @throws IOException
   */
  @RequestMapping(name = "articleAuthors", value = "/article/authors")
  public String renderArticleAuthors(HttpServletRequest request, Model model, @SiteParam Site site,
                                     RequestedDoiVersion articleId) throws IOException {
    articleMetadataFactory.get(site, articleId)
        .validateVisibility("articleAuthors")
        .populate(request, model);
    return site + "/ftl/article/authors";
  }

  /**
   * Serves the article metrics tab content for an article.
   *
   * @param model     data to pass to the view
   * @param site      current site
   * @param articleId specifies the article
   * @return path to the template
   * @throws IOException
   */
  @RequestMapping(name = "articleMetrics", value = "/article/metrics")
  public String renderArticleMetrics(HttpServletRequest request, Model model, @SiteParam Site site,
                                     RequestedDoiVersion articleId) throws IOException {
    articleMetadataFactory.get(site, articleId)
        .validateVisibility("articleMetrics")
        .populate(request, model);
    return site + "/ftl/article/metrics";
  }

  /**
   * Serves the related content tab content for an article.
   *
   * @param model     data to pass to the view
   * @param site      current site
   * @param articleId specifies the article
   * @return path to the template
   * @throws IOException
   */
  @RequestMapping(name = "articleRelatedContent", value = "/article/related")
  public String renderArticleRelatedContent(HttpServletRequest request, Model model, @SiteParam Site site,
                                            RequestedDoiVersion articleId) throws IOException {
    articleMetadataFactory.get(site, articleId)
        .validateVisibility("articleRelatedContent")
        .populate(request, model);
    return site + "/ftl/article/relatedContent";
  }

  /**
   * Serves as a POST endpoint to submit media curation requests
   *
   * @param model data passed in from the view
   * @param site  current site
   * @return path to the template
   * @throws IOException
   */
  @RequestMapping(name = "submitMediaCurationRequest", value = "/article/submitMediaCurationRequest", method = RequestMethod.POST)
  @ResponseBody
  public String submitMediaCurationRequest(HttpServletRequest request, Model model, @SiteParam Site site,
                                           @RequestParam("doi") String doi,
                                           @RequestParam("link") String link,
                                           @RequestParam("comment") String comment,
                                           @RequestParam("title") String title,
                                           @RequestParam("publishedOn") String publishedOn,
                                           @RequestParam("name") String name,
                                           @RequestParam("email") String email,
                                           @RequestParam("consent") String consent)
      throws IOException {
    requireNonemptyParameter(doi);

    if (!link.matches("^\\w+://.*")) {
      link = "http://" + link;
    }

    if (!validateMediaCurationInput(model, link, name, email, title, publishedOn, consent)) {
      //return model for error reporting
      return jsonService.serialize(model);
    }

    String linkComment = name + ", " + email + "\n" + comment;

    List<NameValuePair> params = new ArrayList<>();
    params.add(new BasicNameValuePair("doi", doi.replaceFirst("info:doi/", "")));
    params.add(new BasicNameValuePair("link", link));
    params.add(new BasicNameValuePair("comment", linkComment));
    params.add(new BasicNameValuePair("title", title));
    params.add(new BasicNameValuePair("publishedOn", publishedOn));

    UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, "UTF-8");

    String mediaCurationUrl = (String) site.getTheme().getConfigMap("mediaCuration").get("mediaCurationUrl");
    if (mediaCurationUrl == null) {
      throw new RuntimeException("Media curation URL is not configured");
    }

    HttpPost httpPost = new HttpPost(mediaCurationUrl);
    httpPost.setEntity(entity);
    StatusLine statusLine = null;
    try (CloseableHttpResponse response = cachedRemoteReader.getResponse(httpPost)) {
      statusLine = response.getStatusLine();
    } catch (ServiceRequestException e) {
      //This exception is thrown when the submitted link is already present for the article.
      if (e.getStatusCode() == HttpStatus.CONFLICT.value()
          && e.getResponseBody().equals("The link already exists")) {
        model.addAttribute("formError", "This link has already been submitted. Please submit a different link");
        model.addAttribute("isValid", false);
      } else {
        throw new RuntimeException(e);
      }
    } finally {
      httpPost.releaseConnection();
    }

    if (statusLine != null && statusLine.getStatusCode() != HttpStatus.CREATED.value()) {
      throw new RuntimeException("bad response from media curation server: " + statusLine);
    }

    return jsonService.serialize(model);
  }

  /**
   * Validate the input from the form
   *
   * @param model data passed in from the view
   * @param link  link pointing to media content relating to the article
   * @param name  name of the user submitting the media curation request
   * @param email email of the user submitting the media curation request
   * @return true if everything is ok
   */

  private boolean validateMediaCurationInput(Model model, String link, String name,
                                             String email, String title, String publishedOn, String consent)
      throws IOException {

    boolean isValid = true;

    UrlValidator urlValidator = new UrlValidator(new String[]{"http", "https"});

    if (consent == null || !"true".equals(consent)) {
      model.addAttribute("consentError", "This field is required.");
      isValid = false;
    }

    if (StringUtils.isBlank(link)) {
      model.addAttribute("linkError", "This field is required.");
      isValid = false;
    } else if (!urlValidator.isValid(link)) {
      model.addAttribute("linkError", "Invalid Media link URL");
      isValid = false;
    }

    if (StringUtils.isBlank(name)) {
      model.addAttribute("nameError", "This field is required.");
      isValid = false;
    }

    if (StringUtils.isBlank(title)) {
      model.addAttribute("titleError", "This field is required.");
      isValid = false;
    }

    if (StringUtils.isBlank(publishedOn)) {
      model.addAttribute("publishedOnError", "This field is required.");
      isValid = false;
    } else {
      try {
        LocalDate.parse(publishedOn);
      } catch (DateTimeParseException e) {
        model.addAttribute("publishedOnError", "Invalid Date Format, should be YYYY-MM-DD");
        isValid = false;
      }
    }

    if (StringUtils.isBlank(email)) {
      model.addAttribute("emailError", "This field is required.");
      isValid = false;
    } else if (!EmailValidator.getInstance().isValid(email)) {
      model.addAttribute("emailError", "Invalid e-mail address");
      isValid = false;
    }

    model.addAttribute("isValid", isValid);
    return isValid;
  }

  /*
   * Returns a list of figures and tables of a given article; main usage is the figshare tile on the Metrics
   * tab
   *
   * @param site current site
   * @param articleId DOI identifying the article
   * @return a list of figures and tables of a given article
   * @throws IOException
   */
  @RequestMapping(name = "articleFigsAndTables", value = "/article/assets/figsAndTables")
  public ResponseEntity<List> listArticleFiguresAndTables(@SiteParam Site site,
                                                          RequestedDoiVersion articleId) throws IOException {
    List<Map<String, ?>> figureView = articleMetadataFactory.get(site, articleId)
        .validateVisibility("articleFigsAndTables")
        .getFigureView();

    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
    return new ResponseEntity<>(figureView, headers, HttpStatus.OK);
  }

  @RequestMapping(name = "uploadPreprintRevision", value = "/article/uploadPreprintRevision")
  public String uploadPreprintRevision(HttpServletRequest request, Model model, @SiteParam Site site,
                                       @RequestParam("state") String state,
                                       @RequestParam("code") String code) throws IOException, URISyntaxException {
    final byte[] decodedState = Base64.getDecoder().decode(state);
    final String decodedJson = URLDecoder.decode(new String(decodedState), "UTF-8");
    Map<String, Object> stateJson = gson.fromJson(decodedJson, HashMap.class);

    String correspondingAuthorOrcidId = (String) stateJson.get("orcid_id");
    String authenticatedOrcidId = "";

    try {
      authenticatedOrcidId = orcidApi.getOrcidIdFromAuthorizationCode(site, code);
    } catch (OrcidAuthenticationTokenExpiredException | OrcidAuthenticationTokenReusedException e) {
      model.addAttribute("orcidAuthenticationError", e.getMessage());
    }

    boolean isError = true;
    if (correspondingAuthorOrcidId.equals(authenticatedOrcidId)) {
      model.addAttribute("orcidId", correspondingAuthorOrcidId);
      isError = false;
    } else if (!Strings.isNullOrEmpty(authenticatedOrcidId)) {
      model.addAttribute("orcidAuthenticationError", "ORCID IDs do not match. " +
          "Corresponding author ORCID ID must be used.");
    }

    if (isError) {
      final RequestedDoiVersion articleId = RequestedDoiVersion.of((String) stateJson.get("doi"));
      return renderArticle(request, model, site, articleId);
    } else {
      return site + "/ftl/article/uploadPreprintRevision";
    }
  }

  @RequestMapping(name = "email", value = "/article/email")
  public String renderEmailThisArticle(HttpServletRequest request, Model model, @SiteParam Site site,
                                       RequestedDoiVersion articleId) throws IOException {
    articleMetadataFactory.get(site, articleId)
        .validateVisibility("email")
        .populate(request, model);
    model.addAttribute("maxEmails", MAX_TO_EMAILS);
    return site + "/ftl/article/email";
  }

  /**
   * @param model data passed in from the view
   * @param site  current site
   * @return path to the template
   * @throws IOException
   */
  @RequestMapping(name = "emailPost", value = "/article/email", method = RequestMethod.POST)
  public String emailArticle(HttpServletRequest request, HttpServletResponse response, Model model,
                             @SiteParam Site site,
                             RequestedDoiVersion articleId,
                             @RequestParam("articleUri") String articleUri,
                             @RequestParam("emailToAddresses") String emailToAddresses,
                             @RequestParam("emailFrom") String emailFrom,
                             @RequestParam("senderName") String senderName,
                             @RequestParam("note") String note,
                             @RequestParam(value = "authorPhone", required = false) String authorPhone,
                             @RequestParam(value = "authorAffiliation", required = false) String authorAffiliation)
      throws IOException, MessagingException {
    requireNonemptyParameter(articleUri);

    model.addAttribute("emailToAddresses", emailToAddresses);
    model.addAttribute("emailFrom", emailFrom);
    model.addAttribute("senderName", senderName);
    model.addAttribute("note", note);
    model.addAttribute("articleUri", articleUri);

    List<InternetAddress> toAddresses = Splitter.on(CharMatcher.anyOf("\n\r")).omitEmptyStrings()
        .splitToList(emailToAddresses).stream()
        .map(email -> EmailMessage.createAddress(null /*name*/, email))
        .collect(Collectors.toList());

    Set<String> errors = validateEmailArticleInput(toAddresses, emailFrom, senderName);
    if (applyValidation(response, model, errors)) {
      return renderEmailThisArticle(request, model, site, articleId);
    }

    Map<String, ?> articleMetadata = articleMetadataFactory.get(site, articleId)
        .validateVisibility("emailPost")
        .getIngestionMetadata();

    model.addAttribute("article", articleMetadata);
    model.addAttribute("journalName", site.getJournalName());

    if (honeypotService.checkHoneypot(request, authorPhone, authorAffiliation)) {
      response.setStatus(HttpStatus.CREATED.value());
      return site + "/ftl/article/emailSuccess";
    }

    Multipart content = freemarkerMailService.createContent(site, "emailThisArticle", model);

    String title = articleMetadata.get("title").toString();
    title = title.replaceAll("<[^>]+>", "");

    EmailMessage message = EmailMessage.builder()
        .addToEmailAddresses(toAddresses)
        .setSenderAddress(EmailMessage.createAddress(senderName, emailFrom))
        .setSubject("An Article from " + site.getJournalName() + ": " + title)
        .setContent(content)
        .setEncoding(freeMarkerConfig.getConfiguration().getDefaultEncoding())
        .build();

    message.send(javaMailSender);

    response.setStatus(HttpStatus.CREATED.value());
    return site + "/ftl/article/emailSuccess";
  }

  private Set<String> validateEmailArticleInput(List<InternetAddress> emailToAddresses,
                                                String emailFrom, String senderName) throws IOException {

    Set<String> errors = new HashSet<>();
    if (StringUtils.isBlank(emailFrom)) {
      errors.add("emailFromMissing");
    } else if (!EmailValidator.getInstance().isValid(emailFrom)) {
      errors.add("emailFromInvalid");
    }

    if (emailToAddresses.isEmpty()) {
      errors.add("emailToAddressesMissing");
    } else if (emailToAddresses.size() > MAX_TO_EMAILS) {
      errors.add("tooManyEmailToAddresses");
    } else if (emailToAddresses.stream()
        .noneMatch(email -> EmailValidator.getInstance().isValid(email.toString()))) {
      errors.add("emailToAddressesInvalid");
    }

    if (StringUtils.isBlank(senderName)) {
      errors.add("senderNameMissing");
    }

    return errors;
  }


  @SuppressWarnings("serial")
  static class XmlContent implements Serializable {
    private final String html;
    private final ImmutableList<Reference> references;

    public XmlContent(String html, List<Reference> references) {
      this.html = Objects.requireNonNull(html);
      this.references = ImmutableList.copyOf(references);
    }
  }

  /**
   * Gets article xml from cache if it exists; otherwise, gets it from rhino and caches it. Then it parses the
   * references and does html transform
   *
   * @param articlePointer
   * @param request
   * @return an XmlContent containing the list of references and article html
   * @throws IOException
   */
  private XmlContent getXmlContent(Site site, ArticlePointer articlePointer,
                                   HttpServletRequest request) throws IOException {
    return corpusContentApi.readManuscript(articlePointer, site, "html", (InputStream stream) -> {
      byte[] xml = ByteStreams.toByteArray(stream);
      final Document document = parseXmlService.getDocument(new ByteArrayInputStream(xml));

      // do not supply Solr related link service now
      List<Reference> references = parseXmlService.parseArticleReferences(document, null);

      // invoke the Solr API once to resolve all journal keys
      List<String> dois = references.stream().map(ref -> ref.getDoi()).filter(doi -> inPlosJournal(doi)).collect(Collectors.toList());
      List<String> keys = doiToJournalResolutionService.getJournalKeysFromDois(dois, site);

      // store the link text from journal key to references.
      // since Reference is immutable, need to create a new list of new reference objects.
      Iterator<Reference> itRef = references.iterator();
      Iterator<String> itKey = keys.iterator();
      List<Reference> referencesWithLinks = new ArrayList<Reference>();
      while (itRef.hasNext()) {
        Reference ref = itRef.next();
        if (!inPlosJournal(ref.getDoi())) {
          referencesWithLinks.add(ref);
          continue;
        }

        String key = itKey.next();
        if (Strings.isNullOrEmpty(key)) {
          referencesWithLinks.add(ref);
          continue;
        }

        Reference.Builder builder = new Reference.Builder(ref);
        Reference refWithLink = builder.setFullArticleLink(getLinkText(site, request, ref.getDoi(), key)).build();
        referencesWithLinks.add(refWithLink);
      }

      references = referencesWithLinks;

      StringWriter articleHtml = new StringWriter(XFORM_BUFFER_SIZE);
      try (OutputStream outputStream = new WriterOutputStream(articleHtml, charset)) {
        articleTransformService.transformArticle(site, articlePointer, references,
            new ByteArrayInputStream(xml), outputStream);
      }

      return new XmlContent(articleHtml.toString(), references);
    });
  }

  private Boolean inPlosJournal(String doi) {
    return doi != null && doi.startsWith("10.1371/");
  }

  private String getLinkText(Site site, HttpServletRequest request, String doi, String citationJournalKey) throws IOException {
    String linkText = null;
    if (citationJournalKey != null) {
      linkText = Link.toForeignSite(site, citationJournalKey, siteSet)
          .toPattern(requestMappingContextDictionary, "article")
          .addQueryParameter("id", doi)
          .build()
          .get(request);
    }
    return linkText;
  }
}
