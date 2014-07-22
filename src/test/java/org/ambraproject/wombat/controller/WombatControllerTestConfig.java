package org.ambraproject.wombat.controller;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import freemarker.cache.FileTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import org.ambraproject.wombat.config.site.Site;
import org.ambraproject.wombat.config.site.SiteSet;
import org.ambraproject.wombat.config.site.url.SiteRequestScheme;
import org.ambraproject.wombat.freemarker.Iso8601DateDirective;
import org.ambraproject.wombat.freemarker.RandomIntegerDirective;
import org.ambraproject.wombat.freemarker.ReplaceParametersDirective;
import org.springframework.context.annotation.Bean;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * Created by jkrzemien on 7/16/14.
 */


public class WombatControllerTestConfig extends WebMvcConfigurerAdapter {

    private static SiteResolver resolver = null;
    public static final String MOCK_SITE_URL = ""; //http://unittest.com";

    public TemplateDirectiveModel getEmptyTemplateDirectiveModel() {
        return new TemplateDirectiveModel() {
            @Override
            public void execute(Environment env, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body) throws TemplateException, IOException {
            }
        };
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        super.addArgumentResolvers(argumentResolvers);
        argumentResolvers.add(siteResolverDependency());
    }

    @Bean
    public FreeMarkerConfig freeMarkerConfig(ServletContext servletContext) throws IOException {

        String[] templates = getFreeMarkerTemplateDirsForTest();
        List<TemplateLoader> loaders = new ArrayList<TemplateLoader>();
        for (String template : templates) {
            FileTemplateLoader loader = new FileTemplateLoader(new File(servletContext.getRealPath(template)));
            loaders.add(loader);
        }

        MultiTemplateLoader mtl = new MultiTemplateLoader(loaders.toArray(new TemplateLoader[0]));

        FreeMarkerConfigurer config = new FreeMarkerConfigurer();
        config.setPreTemplateLoaders(mtl);

        ImmutableMap.Builder<String, Object> variables = ImmutableMap.builder();
        variables.put("formatJsonDate", new Iso8601DateDirective());
        variables.put("replaceParams", new ReplaceParametersDirective());
        variables.put("randomInteger", new RandomIntegerDirective());
        variables.put("siteLink", getEmptyTemplateDirectiveModel());
        variables.put("cssLink", getEmptyTemplateDirectiveModel());
        variables.put("renderCssLinks", getEmptyTemplateDirectiveModel());
        variables.put("js", getEmptyTemplateDirectiveModel());
        variables.put("renderJs", getEmptyTemplateDirectiveModel());
        variables.put("buildInfo", getEmptyTemplateDirectiveModel());
        variables.put("fetchHtml", getEmptyTemplateDirectiveModel());
        variables.put("themeConfig", getEmptyTemplateDirectiveModel());
        variables.put("appLink", getEmptyTemplateDirectiveModel());
        config.setFreemarkerVariables(variables.build());
        return config;
    }

    private String[] getFreeMarkerTemplateDirsForTest() {
        return new String[] { "WEB-INF/themes/root", "WEB-INF/themes/mobile" }; //"WEB-INF/themes/desktop",
    }

    @Bean
    public FreeMarkerViewResolver viewResolver(Charset charset) {
        FreeMarkerViewResolver resolver = new FreeMarkerViewResolver();
        resolver.setContentType("text/html;charset=" + charset);
        resolver.setCache(true);
        resolver.setPrefix("");
        resolver.setSuffix(".ftl");
        resolver.setRequestContextAttribute("requestContext");
        return resolver;
    }

    @Bean
    public SiteSet siteSetDependency() {

        Site mockSite = mock(Site.class);
        ImmutableSet<Site> sitesSet = new ImmutableSet.Builder<Site>().add(mockSite).build();
        when(mockSite.getJournalKey()).thenReturn("daJournalKey");

        SiteRequestScheme mockRequestScheme = mock(SiteRequestScheme.class);
        when(mockRequestScheme.isForSite(any(HttpServletRequest.class))).thenReturn(true);

        when(mockSite.getRequestScheme()).thenReturn(mockRequestScheme);
        when(mockSite.toString()).thenReturn(MOCK_SITE_URL);

        SiteSet mockSet = mock(SiteSet.class);
        when(mockSet.getSites()).thenReturn(sitesSet);
        return mockSet;
    }

    @Bean
    public synchronized SiteResolver siteResolverDependency() {
        if (resolver == null) {
            resolver = new SiteResolver();
        }
        return resolver;
    }

    @Bean
    public Charset charsetDependency() {
        return Charsets.UTF_8;
    }

}
