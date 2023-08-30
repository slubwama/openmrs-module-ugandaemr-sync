<div id="apps">
    <a id="ugandaemrsync-get-facility-id" href="${ui.pageLink("admin","maintenance/settings",[show: "Ugandaemrsync"])}" class="button app big">

        <i class="fas fa-cogs"></i>
        Sync Settings
    </a>

    <% if (requestFacility == true) { %>
    <a id="ugandaemrsync-get-facility-id" href="${ui.pageLink("ugandaemrsync","nationalRegistry")}" class="button app big">

        <i class="icon-lock"></i>
        Request Facility ID
    </a>
    <% } %>

    <% if (requestFacility == false) { %>
    <a id="ugandaemrsync-sync-data" href="${ui.pageLink("ugandaemrsync","syncData")}" class="button app big">

        <i class="icon-refresh"></i>

        Sync Data Page
    </a>
    <% } %>
    <% if (requestFacility == false && initialSync == true) { %>

    <a id="ugandaemrsync-generate-initial-data" href="${ui.pageLink("ugandaemrsync","initialDataGeneration")}"
       class="button app big">

        <i class="icon-building"></i>
        Generate Initial Data
    </a>
    <% } %>

    <a id="ugandaemrsync-generate-initial-data" href="${ui.pageLink("ugandaemrsync","initFHIRDataGeneration")}"
       class="button app big">

        <i class="icon-building"></i>
        Generate Initial FHIR Data
    </a>

    <a id="ugandaemrsync-generate-initial-data" href="${ui.pageLink("ugandaemrsync","viralLoadUpload")}"
       class="button app big">

        <i class=" icon-upload"></i>
        Upload Viral Load Results
    </a>
    <a id="ugandaemrsync-generate-initial-data" href="${ui.pageLink("ugandaemrsync","syncTask")}"
       class="button app big">
        <i class="icon-list-ul"></i>
        Sync Task Logs
    </a>

    <a id="ugandaemrsync-generate-initial-data" href="${ui.pageLink("ugandaemrsync","syncTaskType")}"
       class="button app big">

        <i class="icon-list-ul"></i>
        Sync Task Type
    </a>
    <a id="ugandaemrsync-generate-initial-data" href="${ui.pageLink("ugandaemrsync","sendReports")}"
       class="button app big">

        <i class=" icon-upload"></i>
        Send Aggregate Reports
    </a>

    <a id="ugandaemrsync-generate-initial-data" href="${ui.pageLink("ugandaemrsync","syncFhirProfile")}"
       class="button app big">

        <i class="icon-list-ul"></i>
        Sync FHIR Profile
    </a>
    <a id="ugandaemrsync-generate-initial-data" href="${ui.pageLink("ugandaemrsync","syncFhirProfileStatistics")}"
       class="button app big">

        <i class="icon-bar-chart"></i>
        Sync FHIR Profile Statistics
    </a>

    <a id="ugandaemrsync-generate-initial-data" href="${ui.pageLink("ugandaemrsync","sentSms")}"
       class="button app big">
        <i class="icon-list-ul"></i>
        View Sent SMS
    </a>
</div>