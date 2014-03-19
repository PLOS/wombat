from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.common.keys import Keys
from selenium.webdriver.support.ui import Select
from selenium.common.exceptions import NoSuchElementException
import unittest, time, re
from resources import base_url
from resources import journal_key

class FooterTestLinks(unittest.TestCase):
  def setUp(self):
    self.driver = webdriver.Firefox()
    self.driver.implicitly_wait(30)
    self.base_url = base_url
    self.verificationErrors = []
    self.accept_next_alert = True
  
  def test_footer_test_links(self):
    driver = self.driver
    driver.get(self.base_url + "/wombat/" + journal_key + "/")
    try: self.assertEqual("Back to Top", driver.find_element_by_css_selector("span.btn-text").text)
    except AssertionError as e: self.verificationErrors.append(str(e))
    try: self.assertEqual("About Us", driver.find_element_by_link_text("About Us").text)
    except AssertionError as e: self.verificationErrors.append(str(e))
    try: self.assertEqual("Full Site", driver.find_element_by_link_text("Full Site").text)
    except AssertionError as e: self.verificationErrors.append(str(e))
    try: self.assertEqual("Feedback", driver.find_element_by_link_text("Feedback").text)
    except AssertionError as e: self.verificationErrors.append(str(e))
    try: self.assertEqual("Internet Systems Consortium.", driver.find_element_by_link_text("Internet Systems Consortium.").text)
    except AssertionError as e: self.verificationErrors.append(str(e))
    try: self.assertEqual("Privacy Policy", driver.find_element_by_link_text("Privacy Policy").text)
    except AssertionError as e: self.verificationErrors.append(str(e))
    try: self.assertEqual("Terms of Use", driver.find_element_by_link_text("Terms of Use").text)
    except AssertionError as e: self.verificationErrors.append(str(e))
    try: self.assertEqual("Media Inquiries", driver.find_element_by_link_text("Media Inquiries").text)
    except AssertionError as e: self.verificationErrors.append(str(e))
    try: self.assertTrue("regexp:^Version [0-9]\.{2}[0-9].*/[0-9]\.{3}.*", driver.find_element_by_xpath("/html/body/div/footer/p[2]").text)
    except AssertionError as e: self.verificationErrors.append(str(e))
    driver.find_element_by_link_text("Full Site").click()
    try: self.assertTrue(self.is_element_present(By.LINK_TEXT, "create account"))
    except AssertionError as e: self.verificationErrors.append(str(e))
  
  def is_element_present(self, how, what):
    try: self.driver.find_element(by=how, value=what)
    except NoSuchElementException, e: return False
    return True
  
  def is_alert_present(self):
    try: self.driver.switch_to_alert()
    except NoAlertPresentException, e: return False
    return True
  
  def close_alert_and_get_its_text(self):
    try:
      alert = self.driver.switch_to_alert()
      alert_text = alert.text
      if self.accept_next_alert:
        alert.accept()
      else:
        alert.dismiss()
      return alert_text
    finally: self.accept_next_alert = True
  
  def tearDown(self):
    self.driver.quit()
    self.assertEqual([], self.verificationErrors)

if __name__ == "__main__":
  unittest.main()
