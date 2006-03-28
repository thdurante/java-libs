/*
 * component:   "openEHR Reference Implementation"
 * description: "Class Composition"
 * keywords:    "composition"
 *
 * author:      "Rong Chen <rong@acode.se>"
 * support:     "Acode HB <support@acode.se>"
 * copyright:   "Copyright (c) 2004 Acode HB, Sweden"
 * license:     "See notice at bottom of class"
 *
 * file:        "$URL$"
 * revision:    "$LastChangedRevision$"
 * last_change: "$LastChangedDate$"
 */
package org.openehr.rm.composition;

import org.openehr.rm.Attribute;
import org.openehr.rm.FullConstructor;
import org.openehr.rm.common.archetyped.Archetyped;
import org.openehr.rm.common.archetyped.FeederAudit;
import org.openehr.rm.common.archetyped.Link;
import org.openehr.rm.common.archetyped.Locatable;
import org.openehr.rm.support.identification.ObjectID;
import org.openehr.rm.composition.content.navigation.Section;
import org.openehr.rm.datatypes.text.CodePhrase;
import org.openehr.rm.datatypes.text.DvText;
import org.openehr.rm.datatypes.text.DvCodedText;
import org.openehr.rm.support.terminology.TerminologyService;

import java.util.*;

/**
 * A composition is considered the unit of modification of the
 * record, the unit of transmission in record extracts, and the unit
 * of attestation by authorising clinicians. In this latter sense,
 * it may be considered equivalent to a signed document.
 * <p/>
 * Instances of this class are immutable.
 *
 * @author Rong Chen
 * @version 1.0
 */
public final class Composition extends Locatable {

    /**
     * Constructs a Locatable
     *
     * @param uid              null if not specified
     * @param archetypeNodeId
     * @param name
     * @param archetypeDetails null if not specified
     * @param feederAudit      null if not specified
     * @param links            null if not specified
     * @param content          null if not specified
     * @param context
     * @param category
     * @param territory
     * @throws IllegalArgumentException if name null or archetypeNodeId null
     *                                  or links not null and empty
     */
    @FullConstructor
            public Composition(@Attribute(name = "uid") ObjectID uid,
                               @Attribute(name = "archetypeNodeId", required = true) String archetypeNodeId,
                               @Attribute(name = "name", required = true) DvText name,
                               @Attribute(name = "archetypeDetails", required = true) Archetyped archetypeDetails,
                               @Attribute(name = "feederAudit") FeederAudit feederAudit,
                               @Attribute(name = "links") Set<Link> links,
                               @Attribute(name = "content") List<Section> content,
                               @Attribute(name = "context", system = true) EventContext context,
                               @Attribute(name = "category", system = true) DvCodedText category,
                               @Attribute(name = "territory", system = true) CodePhrase territory,
                               @Attribute(name = "terminologyService", system = true) TerminologyService terminologyService) {

        super(uid, archetypeNodeId, name, archetypeDetails, feederAudit,
                links);

        if (!isArchetypeRoot()) {
            throw new IllegalArgumentException("not archetype root");
        }

        if (content != null && content.isEmpty()) {
            throw new IllegalArgumentException("empty content");
        }

        if (isPersistent(category) && context != null) {
            throw new IllegalArgumentException("invalid persistent category");
        }

        // Is_persistent_validity: is_persistent implies context = Void
        if (category == null) {
            throw new IllegalArgumentException("null cateogry");
        }

        // todo: implement this invariant check
        // name_value: not is_persistent implies name.value.is_equal(
        // context. health_care_facility.as_string + context.start_time.as_string)

        if (territory == null) {
            throw new IllegalArgumentException("null territory");
        }
        if (terminologyService == null) {
            throw new IllegalArgumentException("null terminologyService");
        }
        if (!terminologyService.terminology(TerminologyService.OPENEHR)
                .hasCodeForGroupName(category.getDefiningCode(),
                        "composition category", "en")) {
            throw new IllegalArgumentException(
                    "unknown category: " + category.getDefiningCode());
        }
        if (!terminologyService.codeSet("countries").has(territory)) {
            throw new IllegalArgumentException(
                    "unknown territory: " + territory);
        }

        this.content = content;
        this.context = context;
        this.category = category;
        this.territory = territory;
    }

    // check is given category indicates persistent type
    private static boolean isPersistent(DvCodedText category) {
        // todo: implement this
        return false;
    }

    /**
     * The clinical session content of this Composition, ie the
     * information generated in the clinical session.
     *
     * @return unmodibialbe list of section or null if not present
     */
    public List<Section> getContent() {
        return content == null ? null : Collections.unmodifiableList(content);
    }

    /**
     * The clinical session context of this Composition, ie the
     * contextual attributes of the clinical session.
     *
     * @return context
     */
    public EventContext getContext() {
        return context;
    }

    /**
     * Indicates what broad category this Composition is belogs to,
     * eg "persistent" - of longitudinal validity, "event", "process" etc
     *
     * @return category
     */
    public DvCodedText getCategory() {
        return category;
    }

    /**
     * True if category is a "persistent" type, False otherwise. Useful for
     * finding Compositions in an EHR which are guaranteed to be of interest
     * to most users.
     *
     * @return true if persistent
     */
    public boolean isPersistent() {
        return isPersistent(category);
    }

    /**
     * Name of territory in which this Composition was written.
     * Coded from openEHR  "countries"  code set, which is an
     * expression of the ISO 3166 standard.
     *
     * @return territory
     */
    public CodePhrase getTerritory() {
        return territory;
    }

    /**
     * String The path to an item relative to the root of this
     * archetyped structure.
     *
     * @param item
     * @return string path
     */
    public String pathOfItem(Locatable item) {
        return null;  // todo: implement this method
    }

    /**
     * The item at a path that is relative to this item.
     *
     * @param path
     * @return relative path
     * @throws IllegalArgumentException if path invalid
     */
    public Locatable itemAtPath(String path) {
        Locatable ret = super.itemAtPath(path);
        if (ret != null) {
            return ret;
        }
        String whole = whole();
        String tmp = path;
        if (tmp.startsWith(whole)) {
            tmp = tmp.substring(whole.length());
        }
        ret = checkAttribute(tmp, "content", content);
        if (ret == null) {
            throw new IllegalArgumentException("invalid path: " + path);
        }
        return ret;
    }

    /**
     * Return true if the path is valid with respect to the current
     * item.
     *
     * @param path
     * @return true if valid
     */
    public boolean validPath(String path) {
        try {
            itemAtPath(path);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    // POJO start
    Composition() {
    }

    void setContent(List<Section> content) {
        this.content = content;
    }

    void setContext(EventContext context) {
        this.context = context;
    }

    void setCategory(DvCodedText category) {
        this.category = category;
    }

    void setTerritory(CodePhrase territory) {
        this.territory = territory;
    }

    // POJO end

    /* fields */
    private List<Section> content;
    private EventContext context;
    private DvCodedText category;
    private CodePhrase territory;
}

/*
 *  ***** BEGIN LICENSE BLOCK *****
 *  Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 *  The contents of this file are subject to the Mozilla Public License Version
 *  1.1 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.mozilla.org/MPL/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  The Original Code is Composition.java
 *
 *  The Initial Developer of the Original Code is Rong Chen.
 *  Portions created by the Initial Developer are Copyright (C) 2003-2004
 *  the Initial Developer. All Rights Reserved.
 *
 *  Contributor(s):
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 *  ***** END LICENSE BLOCK *****
 */