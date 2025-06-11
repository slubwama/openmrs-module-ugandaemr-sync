<%
    ui.decorateWith("appui", "standardEmrPage", [title: ui.message("ugandaemrsync.title")])
%>

${ ui.includeFragment("ugandaemrsync", "generateInitialFHIRData") }