<%
    // although called "patientDashboard" this is actually the patient visits screen, and clinicianfacing/patient is the main patient dashboard
    ui.decorateWith("appui", "standardEmrPage")
    ui.includeJavascript("uicommons", "bootstrap-collapse.js")
    ui.includeJavascript("uicommons", "bootstrap-transition.js")
    ui.includeCss("uicommons", "styleguide/index.styles")
    ui.includeCss("uicommons", "datatables/dataTables_jui.styles")
    ui.includeJavascript("ugandaemrsync", "synctasktype.js")
%>
<script type="text/javascript">

</script>
<script type="text/javascript">
    var breadcrumbs = [
        { icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm' },
        { label: "${ ui.message("coreapps.app.systemAdministration.label")}", link: '/' + OPENMRS_CONTEXT_PATH + '/coreapps/systemadministration/systemAdministration.page'},
        { label: "UgandaEMR Sync", link: '/' + OPENMRS_CONTEXT_PATH + '/ugandaemrsync/ugandaemrsync.page'},
        { label: "Sent SMS Messages Logs"}
    ];
    var messages;

    jq(document).ready(function () {

        messages =${results}

        if(messages!=="") {
            var tableRow = "";
            for (var i = 0; i < messages.length; i++) {
                var tel_no = messages[i].mobile_no;
                var message = messages[i].message;
                var date = messages[i].date_created;
                var row = "<tr><td>" + tel_no + "</td><td>" + message + "</td><td>" + date + "</td>/tr>";
                tableRow += row;
            }
            jq('#body').append(tableRow);
        }

    });
</script>


<div class="dashboard ">
    <div class="info-section">
        <div class="info-header">
            <label><h2>Sent SMS Messages</h2></label>
        </div>

        <div class="info-body">
            <table id="table" class="table table-striped table-bordered" style="width:100%">
                <thead>
                <tr>
                    <th>Mobile No</th>
                    <th>Message</th>
                    <th>Date Sent</th>
                </tr>
                </thead>
                <tbody id="body">


                </tbody>
            </table>
        </div>
    </div>
</div>
