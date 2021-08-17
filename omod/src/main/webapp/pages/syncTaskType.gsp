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
    var breadcrumbs = [
        {icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm'},
        {
            label: "${ ui.message("coreapps.app.systemAdministration.label")}",
            link: '/' + OPENMRS_CONTEXT_PATH + '/coreapps/systemadministration/systemAdministration.page'
        },
        {label: "UgandaEMR Sync", link: '/' + OPENMRS_CONTEXT_PATH + '/ugandaemrsync/ugandaemrsync.page'},
        {label: "Sync Task Type"}
    ];

    jq(document).ready(function () {

        jq("#okay").click(function () {
            patientqueue.createReadMessageDialog();
        });
    });
</script>

<script>
    if (jQuery) {
        jq(document).ready(function () {
            jq('#addEditSyncTaskTypeModel').on('show.bs.modal', function (event) {
                var button = jq(event.relatedTarget);
                var syncTaskTypeId = button.data('synctaskid');
                var modal = jq(this);

                modal.find("#syncTaskTypeId").val("");
                modal.find("#syncTaskTypeName").val("");
                modal.find("#dataType select").find().val("");
                modal.find("#dataTypeId").val("");
                modal.find("#username").val("");
                modal.find("#password").val("");
                modal.find("#url").val("");
                modal.find("#token").val("");

                jq.get('${ ui.actionLink("ugandaemrsync","syncTaskType","getSyncTaskType",) }', {
                    "syncTaskTypeId": syncTaskTypeId
                }, function (response) {
                    var syncTaskType=JSON.parse(response.replace("syncTaskType=", "\"syncTaskType\":").trim());


                    modal.find("#syncTaskTypeId").val(syncTaskTypeId);
                    modal.find("#syncTaskTypeName").val(syncTaskType.syncTaskType.name);
                    modal.find("#dataType select").find().val(syncTaskType.syncTaskType.dataType);
                    modal.find("#dataTypeId").val(syncTaskType.syncTaskType.dataTypeId);
                    modal.find("#username").val(syncTaskType.syncTaskType.urlUserName);
                    modal.find("#password").val(syncTaskType.syncTaskType.urlPassword);
                    modal.find("#url").val(syncTaskType.syncTaskType.url);
                    modal.find("#token").val(syncTaskType.syncTaskType.urlToken);
                    if (!response) {
                        ${ ui.message("coreapps.none ") }
                    }
                });
            });
        });
    }

    function samuel() {

    }
</script>
<style>
.dashboard .que-container {
    display: inline;
    float: left;
    width: 65%;
    margin: 0 1.04167%;
}

.dashboard .alert-container {
    display: inline;
    float: left;
    width: 30%;
    margin: 0 1.04167%;
}

.dashboard .action-section ul {
    background: #63343c;
    color: white;
    padding: 7px;
}

.card-body {
    -ms-flex: 1 1 auto;
    flex: 7 1 auto;
    padding: 1.0rem;
    background-color: #eee;
}

.my-tab .tab-pane {
    border: solid 1px blue;
}

.vertical {
    border-left: 1px solid #c7c5c5;
    height: 79px;
    position: absolute;
    left: 99%;
    top: 11%;
}

#patient-search {
    min-width: 96%;
    color: #363463;
    display: block;
    padding: 5px 10px;
    height: 45px;
    margin-top: 27px;
    background-color: #FFF;
    border: 1px solid #dddddd;
}
</style>

<div class="card">
    <div class="card-header">
        <div class="">
            <div class="row">
                <div class="col-3">
                    <div>
                        <h2 style="color: maroon">Sync Task Type</h2>
                    </div>

                    <div class="">

                        <button type="button" style="font-size: 25px" class="confirm icon-plus-sign" data-toggle="modal"
                                data-target="#addEditSyncTaskTypeModel"  data-whatever="@mdo">     Create</button>
                    </div>

                    <div class="vertical"></div>
                </div>

                <div class="col-8">
                    <form method="get" id="patient-search-form" onsubmit="return false">
                        <input type="text" id="patient-search"
                               placeholder="${ui.message("coreapps.findPatient.search.placeholder")}"
                               autocomplete="off"/>
                    </form>
                </div>
            </div>
        </div>
    </div>

    <div class="card-body">
        <div class="info-body">
            <table>
                <thead>
                <tr>
                    <th>ID</th>
                    <th>NAME</th>
                    <th>URL</th>
                    <th>Data Type ID</th>
                    <th>DATE</th>
                    <th>UUID</th>
                    <th>ACTION</th>
                </tr>
                </thead>
                <tbody>
                <% if (syncTaskTypes.size() > 0) {
                    syncTaskTypes.each { %>
                <tr>
                    <td>${it.syncTaskTypeId}</td>
                    <td>${it.name}</td>
                    <td>${it.url}</td>
                    <td>${it.dataTypeId}</td>
                    <td>${it.dateCreated}</td>
                    <td>${it.uuid}</td>
                    <td>
                        <i style="font-size: 25px" data-toggle="modal" data-target="#addEditSyncTaskTypeModel" data-synctaskid="${it.uuid}" class="icon-edit edit-action" title="Edit"></i>
                    </td>
                    <% }
                    } %>
                </tr>
                </tbody>
            </table>
        </div>
    </div>
</div>


<div class="modal fade" id="addEditSyncTaskTypeModel" tabindex="-1" role="dialog"
     aria-labelledby="addEditSyncTaskTypeModelLabel"
     aria-hidden="true">
    <div class="modal-dialog  modal-lg" role="document">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title" id="addEditSyncTaskTypeModelLabel">New message</h5>
                <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                    <span aria-hidden="true">&times;</span>
                </button>
            </div>

            <form method="post">
                <input type="hidden" name="syncTaskTypeId" id="syncTaskTypeId" value="">

                <div class="modal-body">
                    <div class="container">
                        <div class="row">
                            <div class="col-6">
                                <div class="form-group">
                                    <label>Sync Task Type Name</label>
                                    <input type="text" class="form-control" id="syncTaskTypeName"
                                           placeholder=" ie Send Tests to Reference lab" name="syncTaskTypeName">
                                </div>

                                <div class="form-group">
                                    <label>Data Type</label>
                                    <select class="form-control" name="dataType" id="dataType">
                                        <option value="">Select Data Type</option>
                                        <option value="java.lang.Boolean">java.lang.Boolean</option>
                                        <option value="java.lang.Character">java.lang.Character</option>
                                        <option value="java.lang.Float">java.lang.Float</option>
                                        <option value="java.lang.Integer">java.lang.Integer</option>
                                        <option value="java.lang.String">java.lang.String</option>
                                        <option value="org.openmrs.Concept">org.openmrs.Concept</option>
                                        <option value="org.openmrs.Drug">org.openmrs.Drug</option>
                                        <option value="org.openmrs.Encounter">org.openmrs.Encounter</option>
                                        <option value="org.openmrs.Order">org.openmrs.Order</option>
                                        <option value="org.openmrs.TestOrder">org.openmrs.TestOrder</option>
                                        <option value="org.openmrs.Location">org.openmrs.Location</option>
                                        <option value="org.openmrs.Patient">org.openmrs.Patient</option>
                                        <option value="org.openmrs.Person">org.openmrs.Person</option>
                                        <option value="org.openmrs.ProgramWorkflow">org.openmrs.ProgramWorkflow</option>
                                        <option value="org.openmrs.Provider">org.openmrs.Provider</option>
                                        <option value="org.openmrs.User">org.openmrs.User</option>
                                        <option value="org.openmrs.util.AttributableDate">org.openmrs.util.AttributableDate</option>
                                    </select>

                                </div>

                                <div class="form-group">
                                    <label>Username</label>
                                    <input type="text" class="form-control" id="username"
                                           placeholder="Username" name="username">
                                </div>
                            </div>


                            <div class="col-6">
                                <div class="form-group">
                                    <label>URL</label>
                                    <input type="text" class="form-control" id="url"
                                           placeholder="url or ip Address to Send Data to" name="url">
                                </div>

                                <div class="form-group">
                                    <label>Data Type Identifier (eg uuid for enounter type)</label>
                                    <input type="text" class="form-control" id="dataTypeId"
                                           placeholder=" ie Send Tests to Reference lab" name="dataTypeId">
                                </div>

                                <div class="form-group">
                                    <label>Password</label>
                                    <input type="password" class="form-control" id="password"
                                           placeholder="Password" name="password">
                                </div>

                                <div class="form-group">
                                    <label>Auth Token</label>
                                    <input type="text" class="form-control" id="token"
                                           placeholder="token" name="token">
                                </div>
                            </div>
                        </div>
                    </div>

                </div>

                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-dismiss="modal">Close</button>
                    <input type="submit" class="confirm" value="${ui.message("Save")}">
                </div>
            </form>
        </div>
    </div>
</div>

