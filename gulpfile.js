/*!
 * Copyright (c) 2017 Public Library of Science
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 */

/*
 * We use gulp-sass to set our sass output details such as directories.
 * To run a watch just use the "gulp" command in your terminal. To run a specific gulp task use the task name following i.e: "gulp build"/
 */

'use strict';

var gulp = require('gulp');
var sass = require('gulp-sass');
var uglify = require('gulp-uglify');
var concat = require('gulp-concat');
var gulpif = require('gulp-if');

gulp.task('sass', function () {
  return gulp.src('src/main/webapp/WEB-INF/themes/desktop/sass/*.scss')
    .pipe(sass({
      sourceComments: 'map',
      sourceMap: 'sass',
      outputStyle: 'compressed'
    }).on('error', sass.logError))
    .pipe(gulp.dest(process.env['OUTPUT_DIR'] + '/WEB-INF/themes/desktop/resource/css'));
});

gulp.task('compress-css', function () {
  return gulp.src('src/main/webapp/**/*.css')
    .pipe(sass({outputStyle: 'compressed'}).on('error', sass.logError))
    .pipe(gulp.dest(process.env['OUTPUT_DIR']));
});

function concatJs(target, srcs) {
  gulp.task(target, function() {
    gulp.src(srcs)
      .pipe(gulpif('!**/*.min.js', uglify({mangle: false})))
      .pipe(concat(target))
      .pipe(gulp.dest(process.env['OUTPUT_DIR'] + 'WEB-INF/themes/root/resource/js/'))
  });
}

concatJs('alm-query.min.js',
         ['src/main/webapp/WEB-INF/themes/root/resource/js/vendor/q.min.js',
          'src/main/webapp/WEB-INF/themes/root/resource/js/util/class.js',
          'src/main/webapp/WEB-INF/themes/root/resource/js/util/error_factory.js',
          'src/main/webapp/WEB-INF/themes/root/resource/js/util/alm_query_promise.js'
         ]);

gulp.task('watch', function () {
  gulp.watch(['**/*.scss', '**/*.css'], ['build']);
});

gulp.task('compress-js', ['alm-query.min.js']);
gulp.task('build', ['sass', 'compress-css', 'compress-js']);
gulp.task('default', ['sass', 'sass:watch', 'compress-css', 'compress-js']);
