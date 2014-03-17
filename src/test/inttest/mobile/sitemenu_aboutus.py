from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.common.keys import Keys
from selenium.webdriver.support.ui import Select
from selenium.common.exceptions import NoSuchElementException
import unittest, time, re

class WombatSitemenuAboutus(unittest.TestCase):
    def setUp(self):
        self.driver = webdriver.Firefox()
        self.driver.implicitly_wait(30)
        self.base_url = "http://one-fluffy.plosjournals.org"
        self.verificationErrors = []
        self.accept_next_alert = True
    
    def test_wombat_sitemenu_aboutus(self):
        driver = self.driver
        driver.get(self.base_url + "/wombat/PlosOne/")
        try: self.assertTrue(self.is_element_present(By.ID, "site-menu-button"))
        except AssertionError as e: self.verificationErrors.append(str(e))
        driver.find_element_by_id("site-menu-button").click()
        driver.find_element_by_xpath("/html/body/div[2]/nav/ul/li[2]/a").click()
        try: self.assertEqual("Journal Information", driver.find_element_by_link_text("Journal Information").text)
        except AssertionError as e: self.verificationErrors.append(str(e))
        try: self.assertEqual("Editorial Board", driver.find_element_by_link_text("Editorial Board").text)
        except AssertionError as e: self.verificationErrors.append(str(e))
        try: self.assertEqual("Reviewer Guidelines", driver.find_element_by_link_text("Reviewer Guidelines").text)
        except AssertionError as e: self.verificationErrors.append(str(e))
        try: self.assertEqual("Article-Level Metrics", driver.find_element_by_link_text("Article-Level Metrics").text)
        except AssertionError as e: self.verificationErrors.append(str(e))
        try: self.assertEqual("Open-Access License", driver.find_element_by_link_text("Open-Access License").text)
        except AssertionError as e: self.verificationErrors.append(str(e))
        try: self.assertEqual("Media Downloads", driver.find_element_by_link_text("Media Downloads").text)
        except AssertionError as e: self.verificationErrors.append(str(e))
        try: self.assertEqual("Guidelines for Comments and Corrections", driver.find_element_by_link_text("Guidelines for Comments and Corrections").text)
        except AssertionError as e: self.verificationErrors.append(str(e))
        try: self.assertEqual("Help Using this Site", driver.find_element_by_link_text("Help Using this Site").text)
        except AssertionError as e: self.verificationErrors.append(str(e))
        try: self.assertEqual("Contact Us", driver.find_element_by_link_text("Contact Us").text)
        except AssertionError as e: self.verificationErrors.append(str(e))
        driver.find_element_by_xpath("//div[@id='common-menu-container']/nav/ul/li[2]/a").click()
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
