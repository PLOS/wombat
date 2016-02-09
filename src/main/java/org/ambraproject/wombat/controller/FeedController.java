package org.ambraproject.wombat.controller;

import org.ambraproject.wombat.rss.WombatFeed;
import org.ambraproject.wombat.rss.WombatRssViewer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class FeedController {

  @Autowired
  private WombatFeed myFeed;

  @Autowired
  private WombatRssViewer wombatRssViewer;

  @RequestMapping(name ="feed", value="/feed.*", method = RequestMethod.GET)
  public ModelAndView getContent() {
    ModelAndView mav = new ModelAndView();
    mav.addObject("feeds", myFeed.createFeed());
    mav.setView(wombatRssViewer);
    return mav;
  }
}