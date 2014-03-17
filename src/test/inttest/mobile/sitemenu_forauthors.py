from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.common.keys import Keys
from selenium.webdriver.support.ui import Select
from selenium.common.exceptions import NoSuchElementException
import unittest, time, re

class WombatSitemenuForauthors(unittest.TestCase):
    def setUp(self):
        self.driver = webdriver.Firefox()
        self.driver.implicitly_wait(30)
        self.base_url = "http://one-fluffy.plosjournals.org/"
        self.verificationErrors = []
        self.accept_next_alert = True
    
    def test_wombat_sitemenu_forauthors(self):
        driver = self.driver
        driver.get(self.base_url + "/wombat/PlosOne/")
        try: self.assertTrue(self.is_element_present(By.ID, "site-menu-button"))
        except AssertionError as e: self.verificationErrors.append(str(e))
        driver.find_element_by_id("site-menu-button").click()
        driver.find_element_by_css_selector("span.arrow").click()
        try: self.assertEqual("Why Publish with PLOS ONE", driver.find_element_by_link_text("Why Publish with PLOS ONE").text)
        except AssertionError as e: self.verificationErrors.append(str(e))
        try: self.assertEqual("Publication Criteria", driver.find_element_by_link_text("Publication Criteria").text)
        except AssertionError as e: self.verificationErrors.append(str(e))
        try: self.assertEqual("Editorial Policies", driver.find_element_by_link_text("Editorial Policies").text)
        except AssertionError as e: self.verificationErrors.append(str(e))
        try: self.assertEqual("Preparing A Manuscript", driver.find_element_by_link_text("Preparing A Manuscript").text)
        except AssertionError as e: self.verificationErrors.append(str(e))
        try: self.assertEqual("Figure and Table Guidelines", driver.find_element_by_link_text("Figure and Table Guidelines").text)
        except AssertionError as e: self.verificationErrors.append(str(e))
        try: self.assertEqual("Supporting Information Guidelines", driver.find_element_by_link_text("Supporting Information Guidelines").text)
        except AssertionError as e: self.verificationErrors.append(str(e))
        try: self.assertEqual("Submitting a Manuscript", driver.find_element_by_link_text("Submitting a Manuscript").text)
        except AssertionError as e: self.verificationErrors.append(str(e))
        driver.find_element_by_css_selector("a.expander").click()
        driver.find_element_by_id("site-menu-button").click()
    
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
