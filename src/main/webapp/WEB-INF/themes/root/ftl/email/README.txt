The ftl/email/ directory contains FreeMarker templates that are used to
generate email message bodies. Unlike the rest of ftl/*, the templates not used
to render HTTP responses.

This directory, and its 'html' and 'ftl' subdirectories, are significant to
org.ambraproject.wombat.service.FreemarkerMailer. It will look for two '*.ftl'
files with the same name in each of those two subdirectories, and use them
together to compose the HTML and plain-text parts of an email.

Each file may be overridden in child themes as normal.
