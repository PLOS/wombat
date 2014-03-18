from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.common.keys import Keys
from selenium.webdriver.support.ui import Select
from selenium.common.exceptions import NoSuchElementException
import unittest, time, re

class HeaderTestSearch(unittest.TestCase):
    def setUp(self):
        self.driver = webdriver.Firefox()
        self.driver.implicitly_wait(30)
        self.base_url = "http://one-fluffy.plosjournals.org/"
        self.verificationErrors = []
        self.accept_next_alert = True
    
    def test_header_test_search(self):
        driver = self.driver
        driver.get(self.base_url + "/wombat/PlosOne/")
        driver.find_element_by_id("site-logo").click()
        try: self.assertEqual("Browse Topics", driver.find_element_by_id("menu-browse").text)
        except AssertionError as e: self.verificationErrors.append(str(e))
        driver.find_element_by_css_selector("span.icon").click()
        try: self.assertTrue(self.is_element_present(By.ID, "search-cancel"))
        except AssertionError as e: self.verificationErrors.append(str(e))
        try: self.assertTrue(self.is_element_present(By.ID, "search-execute"))
        except AssertionError as e: self.verificationErrors.append(str(e))
        driver.find_element_by_id("search-cancel").click()
        try: self.assertEqual("Browse Topics", driver.find_element_by_id("menu-browse").text)
        except AssertionError as e: self.verificationErrors.append(str(e))
        driver.find_element_by_css_selector("span.icon").click()
        driver.find_element_by_id("search-input").clear()
        driver.find_element_by_id("search-input").send_keys("Retinal Vascular Stress")
        driver.find_element_by_id("search-execute").click()
        try: self.assertTrue(self.is_element_present(By.LINK_TEXT, "Diosmin Alleviates Retinal Edema by Protecting the Blood-Retinal Barrier and Reducing Retinal Vascular Permeability during Ischemia/Reperfusion Injury"))
        except AssertionError as e: self.verificationErrors.append(str(e))
        try: self.assertEqual("Retinal Vascular Caliber Is Associated with Cardiovascular Biomarkers of Oxidative Stress and Inflammation: The POLA Study", driver.find_element_by_link_text("Retinal Vascular Caliber Is Associated with Cardiovascular Biomarkers of Oxidative Stress and Inflammation: The POLA Study").text)
        except AssertionError as e: self.verificationErrors.append(str(e))
        try: self.assertTrue(self.is_element_present(By.CSS_SELECTOR, "button.filter-button.coloration-white-on-color"))
        except AssertionError as e: self.verificationErrors.append(str(e))
        driver.find_element_by_id("site-logo").click()
    
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
