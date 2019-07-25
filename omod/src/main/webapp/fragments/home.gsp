<div id="apps">
    <% if (requestFacility == true) { %>
    <a id="ugandaemrsync-get-facility-id" href="/openmrs/ugandaemrsync/getFacility.page" class="button app big">

        <i class="icon-lock"></i>
        Request Facility ID
    </a>
    <% } %>

    <% if (requestFacility == false) { %>

    <a id="ugandaemrsync-sync-data" href="/openmrs/ugandaemrsync/syncData.page" class="button app big">

        <i class="icon-refresh"></i>

        Sync Data Page
    </a>
    <% } %>
    <% if (requestFacility == false && initialSync == true) { %>

    <a id="ugandaemrsync-generate-initial-data" href="/openmrs/ugandaemrsync/initialDataGeneration.page"
       class="button app big">

        <i class="icon-building"></i>
        Generate Initial Data
    </a>
    <% } %>

    <a id="ugandaemrsync-generate-initial-data" href="/openmrs/ugandaemrsync/viralLoadUpload.page"
       class="button app big">

        <i class=" icon-upload"></i>
        Upload Viral Load Results
    </a>
    <a id="ugandaemrsync-generate-initial-data" href="/openmrs/ugandaemrsync/syncTask.page"
       class="button app big">
        <i class="icon-list-ul"></i>
        Sync Task Logs
    </a>

    <a id="ugandaemrsync-generate-initial-data" href="/openmrs/ugandaemrsync/syncTaskType.page"
       class="button app big">

        <i class="icon-list-ul"></i>
        Sync Task Type
    </a>
</div>