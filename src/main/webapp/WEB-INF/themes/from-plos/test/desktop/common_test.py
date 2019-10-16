#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
POM Definition for the Tests common to all (or most) pages
"""
import logging


class CommonTest:
    """
    Common methods for all tests
    """
    @staticmethod
    def validate_text_exact(actual_text, expected_text, message='Incorrect text'):
        """
        The method to assert that actual_text matches expected_text
        :param actual_text: string
        :param expected_text: string
        :param message: text to specify what exactly is incorrect, for example, 'Incorrect title',
          or header, etc. Optional, default value: 'Incorrect text'
        :return: void function
        """
        logging.info('Verifying text {0}:'.format(actual_text))
        assert actual_text == expected_text, \
            '{0}, expected: {1!r}, found: {2!r}'.format(message, expected_text, actual_text)

    @staticmethod
    def validate_text_contains(actual_text, expected_part_text):
        """
        The method to assert that actual_text contains expected_text
        :param actual_text: string
        :param expected_part_text: string
        :return: void function
        """
        logging.info('Verifying text {0}:'.format(actual_text))
        assert expected_part_text in actual_text, \
            'Incorrect text, {0!r} was expected in {1!r}'.format(expected_part_text, actual_text)
