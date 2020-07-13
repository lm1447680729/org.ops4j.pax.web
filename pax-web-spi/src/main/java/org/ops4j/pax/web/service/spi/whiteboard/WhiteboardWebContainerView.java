/*
 * Copyright 2020 OPS4J.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ops4j.pax.web.service.spi.whiteboard;

import java.util.List;

import org.ops4j.pax.web.service.spi.model.OsgiContextModel;
import org.ops4j.pax.web.service.spi.model.elements.ElementModel;
import org.ops4j.pax.web.service.spi.model.elements.FilterModel;
import org.ops4j.pax.web.service.spi.model.elements.ServletModel;
import org.ops4j.pax.web.service.spi.model.elements.WelcomeFileModel;
import org.ops4j.pax.web.service.views.PaxWebContainerView;
import org.osgi.framework.Bundle;

/**
 * <p>SPI interface which is used by pax-web-extender-whiteboard to gain lower-level access to
 * {@link org.ops4j.pax.web.service.WebContainer} managed and lifecycle-scoped to pax-web-runtime bundle.</p>
 *
 * <p>This is a loose (as loose as possible) coupling between pax-web-runtime (an implementation of HttpService
 * specification) and pax-web-extender-whiteboard (an implementation of Whiteboard Service specification).</p>
 */
public interface WhiteboardWebContainerView extends PaxWebContainerView {

	/**
	 * Returns all the contexts (customized to unified {@link OsgiContextModel} internal representation) managed
	 * at Http Service level. These are either bound to given bundle or "shared"
	 * @param bundle
	 * @return
	 */
	List<OsgiContextModel> getOsgiContextModels(Bundle bundle);

	/**
	 * One-stop method to register a {@link javax.servlet.Servlet} described using {@link ServletModel}.
	 * {@link ServletModel} should always be associated with target (one or many) {@link OsgiContextModel}, because
	 * differently than with {@link org.osgi.service.http.HttpService} scenario, contexts are targeted by logical name
	 * (or LDAP selector) and not as any instance.
	 * @param servletModel
	 */
	void registerServlet(ServletModel servletModel);

	/**
	 * Unregistration of {@link ServletModel} using any set criteria.
	 * @param servletModel
	 */
	void unregisterServlet(ServletModel servletModel);

	/**
	 * One-stop method to register a {@link javax.servlet.Filter} described using {@link FilterModel}.
	 * {@link FilterModel} should always be associated with target (one or many) {@link OsgiContextModel}.
	 * @param filterModel
	 */
	void registerFilter(FilterModel filterModel);

	/**
	 * Unregistration of {@link FilterModel} using any set criteria.
	 * @param filterModel
	 */
	void unregisterFilter(FilterModel filterModel);

	/**
	 * Registers welcome files into {@link ElementModel#getContextModels() associated contexts}
	 * @param welcomeFileModel
	 */
	void registerWelcomeFiles(WelcomeFileModel welcomeFileModel);

	/**
	 * Unregisters welcome files
	 * @param welcomeFileModel
	 */
	void unregisterWelcomeFiles(WelcomeFileModel welcomeFileModel);

	/**
	 * Passes Whiteboard-registered (customized) {@link OsgiContextModel} to be managed in
	 * {@link org.ops4j.pax.web.service.WebContainer}. Such {@link OsgiContextModel} should have
	 * {@link org.osgi.service.http.HttpContext} / {@link org.ops4j.pax.web.service.WebContainerContext} configured
	 * directly. That's the requirement, when Whiteboard cedes the management of such context from
	 * pax-web-extender-whiteboard to pax-web-runtime.
	 * @param model
	 */
	void addWhiteboardOsgiContextModel(OsgiContextModel model);

	/**
	 * Removes Whiteboard-registered (customized) {@link OsgiContextModel} from
	 * {@link org.ops4j.pax.web.service.WebContainer}, which may then switch to using "default"
	 * {@link org.osgi.service.http.HttpContext}
	 * @param model
	 */
	void removeWhiteboardOsgiContextModel(OsgiContextModel model);

}
