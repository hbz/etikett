/*Copyright (c) 2015 "hbz"

This file is part of etikett.

etikett is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package helper;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author Jan Schnasse
 *
 */
public class RdfResource {

    String uri = null;
    List<Link> links = new ArrayList<Link>();
    List<RdfResource> resolvedLinks = new ArrayList<RdfResource>();

    /**
     * A new empty RdfResource
     */
    public RdfResource() {

    }

    /**
     * @param uri
     */
    public RdfResource(String uri) {
	this.uri = uri;
    }

    /**
     * @return the subject uri
     */
    public String getUri() {
	return uri;
    }

    /**
     * @param uri
     */
    public void setUri(String uri) {
	this.uri = uri;
    }

    /**
     * @return links
     */
    public List<Link> getLinks() {
	return links;
    }

    /**
     * @param links
     */
    public void setLinks(List<Link> links) {
	this.links = links;
    }

    /**
     * @return resolvedLinks
     */
    public List<RdfResource> getResolvedLinks() {
	return resolvedLinks;
    }

    /**
     * @param resolvedLinks
     */
    public void setResolvedLinks(List<RdfResource> resolvedLinks) {
	this.resolvedLinks = resolvedLinks;
    }

    /**
     * @param l
     */
    public void addLink(Link l) {
	links.add(l);
    }

    /**
     * @param l
     */
    public void addResolvedLink(RdfResource l) {
	resolvedLinks.add(l);
    }

    /**
     * @param links
     */
    public void addLinks(List<Link> links) {
	this.links.addAll(links);
    }

    @Override
    public String toString() {
	ObjectMapper mapper = new ObjectMapper();
	StringWriter w = new StringWriter();
	try {
	    mapper.writeValue(w, this);
	} catch (Exception e) {
	    e.printStackTrace();
	    return super.toString();
	}
	return w.toString();
    }

    /**
     * @return tries to add labels for linked objects using the list of
     *         resolvedLinks
     */
    public RdfResource resolve() {
	for (Link l : links) {
	    if (!l.isLiteral()) {
		l.setObjectLabel(resolve(l));
	    }
	}
	return this;
    }

    private String resolve(Link l) {
	for (RdfResource r : resolvedLinks) {
	    if (l.getObject().equals(r.getUri())) {
		return findLabel(r.getLinks());
	    }
	}
	return null;
    }

    private String findLabel(List<Link> list) {
	for (Link l : list) {
	    if ("http://d-nb.info/standards/elementset/gnd#preferredName"
		    .equals(l.getPredicate()))
		return l.getObject();
	    if ("http://www.w3.org/2004/02/skos/core#prefLabel".equals(l
		    .getPredicate()))
		return l.getObject();
	}
	return null;
    }

}