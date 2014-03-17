from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.common.keys import Keys
from selenium.webdriver.support.ui import Select
from selenium.common.exceptions import NoSuchElementException
import unittest, time, re

class WombatSitemenuSubmitmanuscript(unittest.TestCase):
    def setUp(self):
        self.driver = webdriver.Firefox()
        self.driver.implicitly_wait(30)
        self.base_url = "http://one-fluffy.plosjournals.org/"
        self.verificationErrors = []
        self.accept_next_alert = True
    
    def test_wombat_sitemenu_submitmanuscript(self):
        driver = self.driver
        driver.get(self.base_url + "/wombat/PlosOne/")
        try: self.assertTrue(self.is_element_present(By.ID, "site-menu-button"))
        except AssertionError as e: self.verificationErrors.append(str(e))
        driver.find_element_by_id("site-menu-button").click()
        try: self.assertEqual("Submit Your Manuscript", driver.find_element_by_css_selector("h4.coloration-light-text").text)
        except AssertionError as e: self.verificationErrors.append(str(e))
        try: self.assertEqual("get started", driver.find_element_by_link_text("get started").text)
        except AssertionError as e: self.verificationErrors.append(str(e))
        driver.find_element_by_link_text("get started").click()
        try: self.assertEqual("Submitting a Manuscript", driver.find_element_by_css_selector("h1").text)
        except AssertionError as e: self.verificationErrors.append(str(e))
        driver.get(self.base_url + "/wombat/PlosOne/")
    
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
