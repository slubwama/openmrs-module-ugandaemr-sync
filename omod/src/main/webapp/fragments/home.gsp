<div id="apps">
    <a id="ugandaemrsync-get-facility-id" href="${ui.pageLink("admin","maintenance/settings",[show: "Ugandaemrsync"])}" class="button app big">

        <i class="fas fa-cogs"></i>
        Sync Settings
    </a>

    <% if (requestFacility == true) { %>
    <a id="ugandaemrsync-get-facility-id" href="${ui.pageLink("ugandaemrsync","getFacility")}" class="button app big">

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
</div>