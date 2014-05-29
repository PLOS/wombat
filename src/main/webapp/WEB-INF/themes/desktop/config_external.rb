
# Compass config file for creating external css - all fonts have absolute paths. This is not run automatically right now.
# Require any additional compass plugins here.

# Set this to the root of your project when deployed:
http_path = "/wombat/DesktopPlosBiology/"
css_dir = "resource/css/external"
sass_dir = "sass"
images_dir = "resource/img"
javascripts_dir = "resource/js"
fonts_dir = "resource/fonts"

output_style = :compressed

environment = :development

# To enable relative paths to assets via compass helper functions. Uncomment:
relative_assets = false

# To disable debugging comments that display the original location of your selectors. Uncomment:
line_comments = false
color_output = false


# If you prefer the indented syntax, you might want to regenerate this
# project again passing --syntax sass, or you can uncomment this:
# preferred_syntax = :sass
# and then run:
# sass-convert -R --from scss --to sass sass scss && rm -rf sass && mv scss sass
preferred_syntax = :scss
