package org.openmrs.module.ugandaemrsync.web.resource;

import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.StringProperty;
import org.openmrs.*;
import org.openmrs.api.ConceptService;
import org.openmrs.api.OrderService;
import org.openmrs.api.context.Context;
import org.openmrs.module.ugandaemrsync.api.UgandaEMRSyncService;
import org.openmrs.module.ugandaemrsync.model.SyncTask;
import org.openmrs.module.ugandaemrsync.model.SyncTaskType;
import org.openmrs.module.ugandaemrsync.web.resource.DTO.ReferralOrder;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.RefRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingCrudResource;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResourceDoesNotSupportOperationException;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.openmrs.parameter.OrderSearchCriteria;
import org.openmrs.util.OpenmrsUtil;

import java.util.*;
import java.util.stream.Collectors;

import static org.openmrs.module.ugandaemrsync.server.SyncConstant.*;

@Resource(name = RestConstants.VERSION_1 + "/syncreferralorder", supportedClass = ReferralOrder.class, supportedOpenmrsVersions = {"1.9.* - 9.*"})
public class ReferralOrderResource extends DelegatingCrudResource<ReferralOrder> {

    @Override
    public ReferralOrder newDelegate() {
        throw new ResourceDoesNotSupportOperationException("Operation not supported");
    }

    @Override
    public ReferralOrder save(ReferralOrder ReferralOrder) {
        throw new ResourceDoesNotSupportOperationException("Operation not supported");
    }

    @Override
    public ReferralOrder getByUniqueId(String uniqueId) {

        ReferralOrder referralOrder = new ReferralOrder();

        Order order = Context.getOrderService().getOrderByUuid(uniqueId);

        if (order != null && order.getAccessionNumber() != null) {
            referralOrder.setOrder(order);
            SyncTask syncTask = getSyncTaskByOrder(order);

            if (syncTask != null) {
                referralOrder.setSyncTask(syncTask);
            }
        }
        return referralOrder;
    }

    @Override
    public NeedsPaging<ReferralOrder> doGetAll(RequestContext context) throws ResponseException {
        OrderService orderService = Context.getOrderService();
        CareSetting careSetting = orderService.getCareSettingByUuid(CARE_SETTING_UUID_OPD);

        // Fetch the order type for drug orders
        OrderType orderType = orderService.getOrderTypeByUuid(ORDER_TYPE_TEST_UUID);
        OrderSearchCriteria orderSearchCriteria = new OrderSearchCriteria(
                null,
                careSetting,
                Collections.singletonList(Context.getConceptService().getConcept(165412)),
                Collections.singletonList(orderType),
                null,
                null,
                null,
                null,
                false,
                null,
                null,
                null,
                null,
                true,
                true,
                true,
                false
        );

        List<Order> orders = Context.getOrderService().getOrders(orderSearchCriteria);
        List<ReferralOrder> referralOrders = new ArrayList<>();
        for (Order order : orders) {
            ReferralOrder referralOrder = new ReferralOrder();
            referralOrder.setOrder(order);
            referralOrder.setSyncTask(getSyncTaskByOrder(order));

            referralOrders.add(referralOrder);
        }

        return new NeedsPaging<ReferralOrder>(new ArrayList<ReferralOrder>(referralOrders), context);
    }

    @Override
    public List<Representation> getAvailableRepresentations() {
        return Arrays.asList(Representation.DEFAULT, Representation.FULL);
    }

    @Override
    public DelegatingResourceDescription getRepresentationDescription(Representation rep) {
        if (rep instanceof DefaultRepresentation) {
            DelegatingResourceDescription description = new DelegatingResourceDescription();
            description.addProperty("uuid");
            description.addProperty("order", Representation.REF);
            description.addProperty("syncTask", Representation.REF);
            description.addSelfLink();
            return description;
        } else if (rep instanceof FullRepresentation) {
            DelegatingResourceDescription description = new DelegatingResourceDescription();
            description.addProperty("uuid");
            description.addProperty("order", Representation.REF);
            description.addProperty("syncTask", Representation.REF);
            description.addSelfLink();
            description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
            return description;
        } else if (rep instanceof RefRepresentation) {
            DelegatingResourceDescription description = new DelegatingResourceDescription();
            description.addProperty("uuid");
            description.addProperty("order", Representation.REF);
            description.addProperty("syncTask", Representation.REF);
            description.addSelfLink();
            return description;
        }
        return null;
    }

    @Override
    protected void delete(ReferralOrder syncFhirCase, String s, RequestContext requestContext) throws ResponseException {

    }

    @Override
    public void purge(ReferralOrder syncFhirCase, RequestContext requestContext) throws ResponseException {

    }

    @Override
    public DelegatingResourceDescription getCreatableProperties() throws ResourceDoesNotSupportOperationException {
        DelegatingResourceDescription description = new DelegatingResourceDescription();
        description.addProperty("uuid");
        description.addProperty("order", Representation.REF);
        description.addProperty("syncTask", Representation.REF);

        return description;
    }

    @Override
    protected PageableResult doSearch(RequestContext context) {
        UgandaEMRSyncService syncService = Context.getService(UgandaEMRSyncService.class);
        OrderService orderService = Context.getOrderService();
        ConceptService conceptService = Context.getConceptService();

        String activatedOnOrAfter = context.getParameter("activatedOnOrAfter");
        String fulfillerStatusParam = context.getParameter("fullfilerStatus");
        String synced = context.getParameter("synced");

        Order.FulfillerStatus fulfillerStatus = parseFulfillerStatus(fulfillerStatusParam);

        CareSetting careSetting = orderService.getCareSettingByUuid(CARE_SETTING_UUID_OPD);
        OrderType orderType = orderService.getOrderTypeByUuid(ORDER_TYPE_TEST_UUID);
        Date activatedDate = OpenmrsUtil.firstSecondOfDay(new Date());
        if (activatedOnOrAfter != null && !activatedOnOrAfter.equals("")) {
            activatedDate = OpenmrsUtil.firstSecondOfDay(syncService.getDateFromString(activatedOnOrAfter, "yyyy-MM-dd"));
        }

        OrderSearchCriteria searchCriteria = new OrderSearchCriteria(
                null,
                careSetting,
                Collections.singletonList(conceptService.getConcept(165412)),
                Collections.singletonList(orderType),
                null, null, null, activatedDate,
                false, null, null,
                null, fulfillerStatus,
                true, true, true, false
        );

        List<ReferralOrder> referralOrders = orderService.getOrders(searchCriteria).stream().filter(order -> order.getInstructions().equals("REFER TO CPHL"))
                .map(order -> {
                    ReferralOrder referralOrder = new ReferralOrder();
                    referralOrder.setOrder(order);
                    referralOrder.setSyncTask(getSyncTaskByOrder(order));
                    return referralOrder;
                })
                .collect(Collectors.toList());
        return new NeedsPaging<>(referralOrders, context);
    }

    @Override
    public Model getGETModel(Representation rep) {
        ModelImpl model = (ModelImpl) super.getGETModel(rep);
        if (rep instanceof DefaultRepresentation || rep instanceof FullRepresentation) {
            model.property("uuid", new StringProperty());
        }
        if (rep instanceof DefaultRepresentation) {
            model.property("order", new RefProperty("#/definitions/OrderGetRef"))
                    .property("profile", new RefProperty("#/definitions/SyncTaskGetRef"));

        } else if (rep instanceof FullRepresentation) {
            model.property("order", new RefProperty("#/definitions/OrderGetRef"))
                    .property("profile", new RefProperty("#/definitions/SyncTaskGetRef"));
        }
        return model;
    }

    @Override
    public Model getCREATEModel(Representation rep) {
        ModelImpl model = (ModelImpl) super.getGETModel(rep);
        if (rep instanceof DefaultRepresentation || rep instanceof FullRepresentation) {
            model.property("uuid", new StringProperty());
        }
        if (rep instanceof DefaultRepresentation) {
            model.property("order", new RefProperty("#/definitions/OrderGetRef"))
                    .property("syncTask", new RefProperty("#/definitions/SyncTaskGetRef"));

        } else if (rep instanceof FullRepresentation) {
            model.property("order", new RefProperty("#/definitions/OrderGetRef"))
                    .property("profile", new RefProperty("#/definitions/SyncTaskGetRef"));
        }
        return model;
    }

    @Override
    public Model getUPDATEModel(Representation rep) {
        return new ModelImpl().property("uuid", new StringProperty())
                .property("order", new RefProperty("#/definitions/OrderGetRef"))
                .property("syncTask", new RefProperty("#/definitions/SyncTaskGetRef"));
    }

    private SyncTask getSyncTaskByOrder(Order order) {
        if (order == null || order.getAccessionNumber() == null) {
            return null;
        }
        UgandaEMRSyncService syncService = Context.getService(UgandaEMRSyncService.class);
        SyncTaskType syncTaskType = syncService.getSyncTaskTypeByUUID(VIRAL_LOAD_SYNC_TYPE_UUID);
        List<SyncTask> syncTaskLog = syncService.getSyncTasksBySyncTaskId(order.getAccessionNumber()).stream().filter(syncTask -> syncTask.getSyncTaskType().equals(syncTaskType)).collect(Collectors.toList());
        if (!syncTaskLog.isEmpty()) {
            return syncTaskLog.get(0);
        }
        return null;
    }

    private Order.FulfillerStatus parseFulfillerStatus(String status) {
        if (status == null) {
            return null;
        }
        switch (status.toUpperCase()) {
            case "RECEIVED":
                return Order.FulfillerStatus.RECEIVED;
            case "COMPLETED":
                return Order.FulfillerStatus.COMPLETED;
            case "IN_PROGRESS":
                return Order.FulfillerStatus.IN_PROGRESS;
            default:
                return null;
        }
    }
}
