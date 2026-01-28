/*
 * This file is part of Dependency-Track.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * Copyright (c) OWASP Foundation. All Rights Reserved.
 */
package org.dependencytrack.apiserver;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.dependencytrack.apiserver.model.Analysis;
import org.dependencytrack.apiserver.model.ApiKey;
import org.dependencytrack.apiserver.model.BomUploadRequest;
import org.dependencytrack.apiserver.model.CreateNotificationRuleRequest;
import org.dependencytrack.apiserver.model.CreateTeamRequest;
import org.dependencytrack.apiserver.model.CreateVulnerabilityRequest;
import org.dependencytrack.apiserver.model.EventProcessingResponse;
import org.dependencytrack.apiserver.model.Finding;
import org.dependencytrack.apiserver.model.NotificationPublisher;
import org.dependencytrack.apiserver.model.NotificationRule;
import org.dependencytrack.apiserver.model.Project;
import org.dependencytrack.apiserver.model.Team;
import org.dependencytrack.apiserver.model.UpdateExtensionConfigRequest;
import org.dependencytrack.apiserver.model.UpdateNotificationRuleRequest;
import org.dependencytrack.apiserver.model.VulnerabilityPolicy;
import org.dependencytrack.apiserver.model.WorkflowState;
import org.dependencytrack.apiserver.model.WorkflowTokenResponse;

import java.util.List;
import java.util.UUID;

@Path("/api")
public interface ApiServerClient {

    @POST
    @Path("/v1/user/forceChangePassword")
    @Produces(MediaType.WILDCARD)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    void forcePasswordChange(
            @FormParam("username") String username,
            @FormParam("password") String password,
            @FormParam("newPassword") String newPassword,
            @FormParam("confirmPassword") String confirmPassword);

    @POST
    @Path("/v1/user/login")
    @Produces(MediaType.WILDCARD)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    String login(
            @FormParam("username") String username,
            @FormParam("password") String password);

    @PUT
    @Path("/v1/team")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    Team createTeam(CreateTeamRequest request);

    @PUT
    @Path("/v1/team/{uuid}/key")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.WILDCARD)
    ApiKey createApiKey(@PathParam("uuid") UUID teamUuid);

    @POST
    @Path("/v1/permission/{permission}/team/{uuid}")
    @Produces(MediaType.WILDCARD)
    @Consumes(MediaType.APPLICATION_JSON)
    Team addPermissionToTeam(
            @PathParam("uuid") UUID teamUuid,
            @PathParam("permission") String permission);

    @PUT
    @Path("/v1/bom")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    WorkflowTokenResponse uploadBom(BomUploadRequest request);

    @GET
    @Path("/v1/event/token/{token}")
    @Produces(MediaType.WILDCARD)
    @Consumes(MediaType.WILDCARD)
    EventProcessingResponse isEventBeingProcessed(@PathParam("token") String token);

    @PUT
    @Path("/v1/vulnerability")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    void createVulnerability(CreateVulnerabilityRequest request);

    @GET
    @Path("/v1/finding/project/{uuid}")
    @Produces(MediaType.WILDCARD)
    @Consumes(MediaType.APPLICATION_JSON)
    List<Finding> getFindings(
            @PathParam("uuid") UUID projectUuid,
            @QueryParam("suppressed") boolean includeSuppressed);

    @GET
    @Path("/v1/project/lookup")
    @Produces(MediaType.WILDCARD)
    @Consumes(MediaType.APPLICATION_JSON)
    Project lookupProject(
            @QueryParam("name") String name,
            @QueryParam("version") String version);

    @GET
    @Path("/v1/notification/publisher")
    @Produces(MediaType.WILDCARD)
    @Consumes(MediaType.APPLICATION_JSON)
    List<NotificationPublisher> getAllNotificationPublishers();

    @PUT
    @Path("/v1/notification/rule")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    NotificationRule createNotificationRule(CreateNotificationRuleRequest request);

    @POST
    @Path("/v1/notification/rule")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    NotificationRule updateNotificationRule(UpdateNotificationRuleRequest request);

    @GET
    @Path("/v1/policy/vulnerability")
    @Produces(MediaType.WILDCARD)
    @Consumes(MediaType.APPLICATION_JSON)
    List<VulnerabilityPolicy> getAllVulnerabilityPolicies();

    @POST
    @Path("/v1/policy/vulnerability/bundle/sync")
    @Produces(MediaType.WILDCARD)
    @Consumes(MediaType.APPLICATION_JSON)
    WorkflowTokenResponse triggerVulnerabilityPolicyBundleSync();

    @GET
    @Path("/v1/analysis")
    @Produces(MediaType.WILDCARD)
    @Consumes(MediaType.APPLICATION_JSON)
    Analysis getAnalysis(
            @QueryParam("project") UUID projectUuid,
            @QueryParam("component") UUID componentUuid,
            @QueryParam("vulnerability") UUID vulnUuid);

    @POST
    @Path("/v1/finding/project/{uuid}/analyze")
    @Produces(MediaType.WILDCARD)
    @Consumes(MediaType.APPLICATION_JSON)
    WorkflowTokenResponse analyzeProject(@PathParam("uuid") UUID projectUuid);

    @GET
    @Path("/v1/workflow/token/{token}/status")
    List<WorkflowState> getWorkflowStatus(@PathParam("token") String token);

    @PUT
    @Path("/v2/extension-points/{extensionPoint}/extensions/{extension}/config")
    @Produces(MediaType.WILDCARD)
    @Consumes(MediaType.APPLICATION_JSON)
    void updateExtensionConfig(
            @PathParam("extensionPoint") String extensionPoint,
            @PathParam("extension") String extension,
            UpdateExtensionConfigRequest request);

}
