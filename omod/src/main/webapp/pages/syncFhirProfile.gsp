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
        {label: "Sync FHIR Profile"}
    ];

    jq(document).ready(function () {
    });
</script>

<script>
    if (jQuery) {
        jq(document).ready(function () {
            jq('#addEditSyncFhirProfileModel').on('show.bs.modal', function (event) {
                var button = jq(event.relatedTarget);
                var profileId = button.data('synctaskid');
                var modal = jq(this);

                modal.find("#profileId").val("");
                modal.find("#syncFhirProfileName").val("");
                modal.find("#dataType select").find().val("");
                modal.find("#dataTypeId").val("");
                modal.find("#username").val("");
                modal.find("#password").val("");
                modal.find("#url").val("");
                modal.find("#token").val("");

                jq.get('${ ui.actionLink("ugandaemrsync","syncFhirProfile","getSyncFhirProfile",) }', {
                    "profileId": profileId
                }, function (response) {
                    var syncFhirProfile = JSON.parse(response.replace("syncFhirProfile=", "\"syncFhirProfile\":").trim());

                    modal.find("#profileId").val(profileId);
                    modal.find("#syncFhirProfileName").val(syncFhirProfile.syncFhirProfile.name);
                    modal.find("#dataType select").find().val(syncFhirProfile.syncFhirProfile.dataType);
                    modal.find("#dataTypeId").val(syncFhirProfile.syncFhirProfile.dataTypeId);
                    modal.find("#username").val(syncFhirProfile.syncFhirProfile.urlUserName);
                    modal.find("#password").val(syncFhirProfile.syncFhirProfile.urlPassword);
                    modal.find("#url").val(syncFhirProfile.syncFhirProfile.url);
                    modal.find("#token").val(syncFhirProfile.syncFhirProfile.urlToken);
                    if (!response) {
                        ${ ui.message("coreapps.none ") }
                    }
                });
            });
        });
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
                        <h2 style="color: maroon">Sync FHIR Profile</h2>
                    </div>

                    <div class="">

                        <button type="button" style="font-size: 25px" class="confirm icon-plus-sign" data-toggle="modal"
                                data-target="#addEditSyncFhirProfileModel" data-whatever="@mdo"> Create </button>
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
                    <th>UUID</th>
                    <th>ACTION</th>
                </tr>
                </thead>
                <tbody>
                <% if (syncFhirProfiles?.size() > 0) {
                    syncFhirProfiles?.each { %>
                <tr>
                    <td>${it?.syncFhirProfileId}</td>
                    <td>${it?.name}</td>
                    <td>${it?.url}</td>
                    <td>${it?.uuid}</td>
                    <td>
                        <i style="font-size: 25px" data-toggle="modal" data-target="#addEditSyncFhirProfileModel"
                           data-synctaskid="${it.uuid}" class="icon-edit edit-action" title="Edit"></i>
                    </td>
                    <% }
                    } %>
                </tr>
                </tbody>
            </table>
        </div>
    </div>
</div>


<div class="modal fade" id="addEditSyncFhirProfileModel" tabindex="-1" role="dialog"
     aria-labelledby="addEditSyncFhirProfileModelLabel"
     aria-hidden="true">
    <div class="modal-dialog  modal-lg" role="document">

        <form method="post">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title" id="addEditSyncFhirProfileModelLabel">New Sync FHIR Profile</h5>
                    <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                        <span aria-hidden="true">&times;</span>
                    </button>
                </div>

                <div class="container">
                    <!-- Content here -->
                    <input type="hidden" name="profileId" id="profileId" value="">
                    <input type="hidden" name="resourceSearchParameter" id="resourceSearchParameter" value="">

                    <div class="container">
                        <ul class="nav nav-tabs nav-fill card-header-tabs">
                            <li class="nav-item">
                                <a class="nav-link active" data-toggle="tab"
                                   href="#resource-definition-section">Resource Definition</a>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link" data-toggle="tab"
                                   href="#resource-filters-section">Resource Filters</a>
                            </li>
                            <li class="nav-item">
                                <a class="nav-link" data-toggle="tab"
                                   href="#sync-settings-section">Sync Settings</a>
                            </li>
                        </ul>
                        <br/>
                    </div>

                    <div class="tab-content">
                        <div class="tab-pane active" id="resource-definition-section">
                            <div class="container" style="margin-left: auto; margin-right: auto; width: 99%">
                                <div class="row">
                                    <div class="col-12">
                                        <div class="card">
                                            <div class="card-header">Resource Definition</div>

                                            <div class="card-body">
                                                <div class="form-group">
                                                    <label>Sync FHIR Profile Name</label>
                                                    <input type="text" class="form-control" id="syncFhirProfileName"
                                                           placeholder="The Name of the Profile"
                                                           name="syncFhirProfileName">
                                                </div>

                                                <div class="form-check">
                                                    <input type="checkbox" id="generateBundle"
                                                           name="generateBundle"
                                                           value="true">

                                                    <label class="form-check-label"
                                                           for="generateBundle">
                                                        Generate Bundle
                                                    </label>
                                                </div>

                                                <div class="form-group">
                                                    <label>No of Resources in Bundle</label>
                                                    <input type="number" class="form-control"
                                                           id="noOfResourcesInBundle"
                                                           name="noOfResourcesInBundle">
                                                </div>

                                                <div class="form-group">
                                                    <label>Duration To Keep Synced Resource (Days)</label>
                                                    <input type="number" class="form-control"
                                                           id="durationToKeepSyncedResources"
                                                           name="durationToKeepSyncedResources">
                                                </div>
                                            </div>
                                        </div>

                                        <div class="card">
                                            <div class="card-header">Resource Type</div>

                                            <div class="card-body">

                                                <div class="">
                                                    <label style="font-weight: bolder">Resource Type</label>

                                                    <div class="row">
                                                        <div class="col-sm-6">
                                                            <div class="form-group">
                                                                <div class="form-check">
                                                                    <input type="checkbox"
                                                                           id="resourceTypeEncounter"
                                                                           name="resourceType"
                                                                           class="resourceType"
                                                                           value="Encounter">
                                                                    <label class="form-check-label"
                                                                           for="resourceTypeEncounter">
                                                                        Encounter
                                                                    </label>
                                                                </div>


                                                                <div class="form-check">
                                                                    <input type="checkbox" id="resourcePatient"
                                                                           name="resourceType"
                                                                           value="Patient" class="resourceType">
                                                                    <label class="form-check-label "
                                                                           for="resourcePatient">
                                                                        Patient
                                                                    </label>
                                                                </div>

                                                                <div class="form-check">
                                                                    <input type="checkbox" id="resourceTypePerson"
                                                                           name="resourceType"
                                                                           class="resourceType"
                                                                           value="Person">
                                                                    <label class="form-check-label"
                                                                           for="resourceTypePerson">
                                                                        Person
                                                                    </label>
                                                                </div>

                                                                <div class="form-check">
                                                                    <input type="checkbox"
                                                                           id="resourceTypeObservation"
                                                                           name="resourceType"
                                                                           class="resourceType"
                                                                           value="Observation">
                                                                    <label class="form-check-label"
                                                                           for="resourceTypeObservation">
                                                                        Observation
                                                                    </label>
                                                                </div>
                                                            </div>
                                                        </div>

                                                        <div class="col-sm-6">
                                                            <div class="form-group">
                                                                <div class="form-check">
                                                                    <input type="checkbox"
                                                                           id="resourceTypeEpisodeOfCare"
                                                                           name="resourceType"
                                                                           class="resourceType"
                                                                           value="EpisodeOfCare">
                                                                    <label class="form-check-label"
                                                                           for="resourceTypeEpisodeOfCare">
                                                                        EpisodeOfCare (Program)
                                                                    </label>
                                                                </div>

                                                                <div class="form-check">
                                                                    <input type="checkbox"
                                                                           id="resourceTypeServiceRequest"
                                                                           name="resourceType"
                                                                           class="resourceType"
                                                                           value="ServiceRequest">
                                                                    <label class="form-check-label"
                                                                           for="resourceTypeServiceRequest">
                                                                        ServiceRequest (Lab Orders)
                                                                    </label>
                                                                </div>

                                                                <div class="form-check">
                                                                    <input type="checkbox"
                                                                           id="resourceTypeMedicationRequest"
                                                                           name="resourceType"
                                                                           class="resourceType"
                                                                           value="MedicationRequest">
                                                                    <label class="form-check-label"
                                                                           for="resourceTypeMedicationRequest">
                                                                        MedicationRequest (Medication Orders)
                                                                    </label>
                                                                </div>

                                                            </div>
                                                        </div>
                                                    </div>
                                                </div>

                                            </div>

                                        </div>
                                    </div>
                                </div>

                                <div class="row">
                                    <div class="col-12">
                                        <div class="card">
                                            <div class="card-header">Case Based Settings</div>

                                            <div class="card-body">
                                                <div class="form-check">
                                                    <input type="checkbox" id="isCaseBasedProfile"
                                                           name="isCaseBasedProfile"
                                                           value="true">

                                                    <label class="form-check-label"
                                                           for="isCaseBasedProfile">
                                                        Is Profile Case Based
                                                    </label>
                                                </div>

                                                <div class="form-group">
                                                    <label>Case Based Primary Resource Type</label>
                                                    <select class="form-control" name="caseBasedPrimaryResourceType"
                                                            id="caseBasedPrimaryResourceType">
                                                        <option value="">Select Primary Resource Type</option>
                                                        <option value="EpisodeOfCare">Episode of Care (Program)</option>
                                                        <option value="Encounter">Encounter</option>
                                                    </select>
                                                </div>

                                                <div class="form-group">
                                                    <label>Case Based Primary Resource Type Identifier</label>
                                                    <input type="text" class="form-control"
                                                           id="caseBasedPrimaryResourceUUID"
                                                           placeholder="UUID of primary resource Type"
                                                           name="caseBasedPrimaryResourceUUID">
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>

                        </div>

                        <div class="tab-pane" id="resource-filters-section">
                            <div class="container" style="margin-left: auto; margin-right: auto; width: 99%">
                                <div class="row">
                                    <div class="col-12">
                                        <div class="card">
                                            <div class="card-header">General Filters</div>

                                            <div class="card-body">
                                                <div class="form-group">
                                                    <label>Patient Identifier Type</label>
                                                    <select class="form-control" name="patientIdentifierType"
                                                            id="patientIdentifierType">
                                                        <option value="">Select Patient Identifier Type</option>
                                                        <% patientIdentifierType?.each { %>
                                                        <option value="${it.uuid}">${it.name}</option>
                                                        <% } %>
                                                    </select>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>

                                <div class="row">
                                    <div class="col-12">
                                        <div class="card">
                                            <div class="card-header">Resource Filters</div>

                                            <div class="card-body">
                                                <div class="form-group">
                                                    <label>Encounter Type UUIDS</label>
                                                    <input type="text" class="form-control resourceTypeFilter"
                                                           id="encounterTypeUUIDS"
                                                           placeholder="comma separate encouter type uuids"
                                                           name="encounterTypeUUIDS">
                                                </div>

                                                <div class="form-group">
                                                    <label>Observation Concept  UUIDS</label>
                                                    <input type="text" class="form-control resourceTypeFilter"
                                                           id="observationCodeUUIDS"
                                                           placeholder="comma separate concept  uuids"
                                                           name="observationCodeUUIDS">
                                                </div>

                                                <div class="form-group">
                                                    <label>EpisodeOfCare (Program)  UUIDS</label>
                                                    <input type="text" class="form-control resourceTypeFilter"
                                                           id="episodeOfCareUUIDS"
                                                           placeholder="comma separate program  uuids"
                                                           name="episodeOfCareUUIDS">
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>

                        <div class="tab-pane" id="sync-settings-section">
                            <div class="container" style="margin-left: auto; margin-right: auto; width: 99%">
                                <div class="row">
                                    <div class="col-12">
                                        <div class="card">
                                            <div class="card-header">Authentication and Authorization</div>

                                            <div class="card-body">

                                                <div class="form-group">
                                                    <label>URL</label>
                                                    <input type="text" class="form-control" id="url"
                                                           placeholder="url or ip Address to Send Data to"
                                                           name="url">
                                                </div>

                                                <div class="form-group">
                                                    <label>Username</label>
                                                    <input type="text" class="form-control" id="username"
                                                           placeholder="Username" name="username">
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
                            </div>
                        </div>
                    </div>

                    <div class="modal-footer">
                        <button type="button" class="btn btn-danger" data-dismiss="modal">Close</button>
                        <input type="submit" class="confirm" value="${ui.message("Save")}">
                    </div>
                </div>
            </div>
        </form>
    </div>
</div>

