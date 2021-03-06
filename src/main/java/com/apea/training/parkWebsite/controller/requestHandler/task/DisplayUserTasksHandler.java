package com.apea.training.parkWebsite.controller.requestHandler.task;

import com.apea.training.parkWebsite.controller.AppAssets;
import com.apea.training.parkWebsite.controller.requestHandler.RequestHandler;
import com.apea.training.parkWebsite.controller.utils.ControllerUtils;
import com.apea.training.parkWebsite.domain.Task;
import com.apea.training.parkWebsite.domain.User;
import com.apea.training.parkWebsite.service.impl.ServiceFactoryImpl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DisplayUserTasksHandler implements RequestHandler {

    @Override
    public String handle(HttpServletRequest request, HttpServletResponse response) {
        AppAssets assets = AppAssets.getInstance();

        if (ControllerUtils.getCurrentUserId(request) == null) {return REDIRECT + assets.get("LOGIN_PAGE");}

        User currentUser = ControllerUtils.getCurrentUser(request);
        Map<Task, String> sendedTasksMap = new HashMap<>();
        List<Task> receivedTasks = new ArrayList<>();
        ServiceFactoryImpl.getInstance().getTaskService().getUserTasks(currentUser)
                .forEach(t -> {
                    if (t.getReceiverId().equals(currentUser.getId())) {
                        receivedTasks.add(t);
                    } else {
                        sendedTasksMap.put(t, ServiceFactoryImpl.getInstance().getCredentialService()
                                .getByUserId(t.getReceiverId()).getLogin());
                    }
                });
        long newReceived = receivedTasks
                .stream()
                .filter(task -> task.getState() == Task.State.NEW)
                .count();
        request.setAttribute(assets.get("CURRENT_USER_SENDED_TASKS_ATTR_NAME"), sendedTasksMap);
        request.setAttribute(assets.get("CURRENT_USER_RECEIVED_TASKS_ATTR_NAME"), receivedTasks);
        request.setAttribute(assets.get("NEW_RECEIVED_COUNT_ATTR_NAME"), newReceived);
        return FORWARD + assets.get("CURRENT_USER_TASKS_LIST_VIEW_NAME");
    }
}