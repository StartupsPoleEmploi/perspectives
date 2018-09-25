"use strict";

$(document).ready(function () {
    var body = $("body");

    body.on("click", ".js-declarerRepriseEmploi", function (e) {
        e.preventDefault();
        var bouton = $(this);
        $.ajax({
            type: "GET",
            url: bouton.attr("href"),
            dataType: "text"
        }).done(function () {
            bouton.hide();
            bouton.parent().html("Non");
        });
    });
});

var app = new Vue({
    el: '#listeCandidats'
});