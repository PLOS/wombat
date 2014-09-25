//DEPENDENCY: resource/js/vendor/jquery.menu-aim.js

var menuAimInit = function() {
  var $menu = $(".dload-menu");

  $menu.menuAim({
    activate: activateSubmenu,
    deactivate: deactivateSubmenu
  });

  function activateSubmenu(row) {
    var $row = $(row),
      submenuId = $row.data("submenuId"),
      $submenu = $("#" + submenuId),
      height = $menu.outerHeight(),
      width = $menu.outerWidth();

    // Show the submenu
    $submenu.css({
      display: "block",
      top: 15,
      left: 0,  // main should overlay submenu
      height: height + 5
    });

    // Keep the currently activated link's highlighted look
    $row.find("a").addClass("maintainHover");
  }

  function deactivateSubmenu(row) {
    var $row = $(row),
      submenuId = $row.data("submenuId"),
      $submenu = $("#" + submenuId);

    // Hide the submenu and remove the link's highlighted look
    $submenu.css("display", "none");
    $row.find("a").removeClass("maintainHover");
  }

  $(document).click(function() {
    $(".dload-xml").css("display", "none");
    $("a.maintainHover").removeClass("maintainHover");
  });
} 
