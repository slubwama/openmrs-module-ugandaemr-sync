<%
    ui.decorateWith("appui", "standardEmrPage", [title: ui.message("Send Reports Page")])
    ui.includeJavascript("uicommons", "angular.min.js")
    ui.includeJavascript("reportingui", "runReport.js")

    def htmlSafeId = { extension ->
        "${extension.id.replace(".", "-")}-${extension.id.replace(".", "-")}-extension"
    }
    def breadcrumbMiddle = breadcrumbOverride ?: '';
%>

${ ui.includeFragment("ugandaemrsync", "sendReports") }