/*
 *  Copyright (C) 2012 VMware, Inc. All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.wavemaker.tools.io;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.Assert;

/**
 * Builder class that can be used to easily construct {@link ResourceFilter}s. Filters can be built for {@link File}s,
 * {@link Folder}s or {@link Resource}s with matching performed on {@link Resource#getName() names} or
 * {@link Resource#toString() paths}. Builders can be chained together to form compound (AND) matches.
 * 
 * @see ResourceFilter
 * 
 * @author Phillip Webb
 */
public abstract class FilterOn {

    /**
     * Start filtering based on {@link Resource} {@link Folder#getName() names}. NOTE: matching is case insensitive.
     * 
     * @return the filter
     */
    public static AttributeFilter names() {
        return getFor(ResourceAttribute.NAME_IGNORING_CASE);
    }

    /**
     * Start filtering based on {@link Resource} {@link Folder#getName() names}. NOTE: matching is case insensitive.
     * 
     * @return the filter
     */
    public static AttributeFilter caseSensitiveNames() {
        return getFor(ResourceAttribute.NAME);
    }

    /**
     * Start filtering based on {@link Resource} {@link Folder#toString() paths}. NOTE: matching is case insensitive.
     * 
     * @return the filter
     */
    public static AttributeFilter paths() {
        return getFor(ResourceAttribute.PATH_IGNORING_CASE);
    }

    /**
     * Start filtering based on {@link Resource} {@link Folder#toString() paths}. NOTE: matching is case insensitive.
     * 
     * @return the filter
     */
    public static AttributeFilter caseSensitivePaths() {
        return getFor(ResourceAttribute.PATH);
    }

    /**
     * Start filtering based on the specified resource type and attribute.
     * 
     * @param resourceType the resource type
     * @param attribute the attribute
     * @return the filter
     */
    public static AttributeFilter getFor(ResourceAttribute attribute) {
        Assert.notNull(attribute, "Attribute must not be null");
        return new AttributeFilter(attribute);
    }

    /**
     * Filter all hidden resources (ie resource names starting '.')
     * 
     * @return the filter
     */
    public static ResourceFilter hidden() {
        return names().starting(".");
    }

    /**
     * Filter all non-hidden resources (ie resource names not starting '.');
     * 
     * @return the filter
     */
    public static ResourceFilter nonHidden() {
        return names().notStarting(".");
    }

    /**
     * Various attributes that can be used to filter resources.
     */
    public enum ResourceAttribute {

        NAME(false) {

            @Override
            public String get(Resource resource) {
                return resource.getName();
            }
        },
        NAME_IGNORING_CASE(true) {

            @Override
            public String get(Resource resource) {
                return resource.getName();
            }
        },
        PATH(false) {

            @Override
            public String get(Resource resource) {
                return resource.toString();
            }
        },
        PATH_IGNORING_CASE(true) {

            @Override
            public String get(Resource resource) {
                return resource.toString();
            }
        };

        private final boolean ignoreCase;

        private ResourceAttribute(boolean ignoreCase) {
            this.ignoreCase = ignoreCase;
        }

        public abstract String get(Resource resource);

        public boolean isIgnoreCase() {
            return this.ignoreCase;
        }
    }

    /**
     * The {@link ResourceFilter} and builder used to further restrict filtering.
     */
    public static class AttributeFilter implements ResourceFilter {

        private final ResourceAttribute attribute;

        private AttributeFilter parent;

        private ResourceFilter filter;

        public AttributeFilter(ResourceAttribute attribute) {
            this.attribute = attribute;
        }

        public AttributeFilter(AttributeFilter parent, ResourceFilter filter) {
            this.parent = parent;
            this.attribute = parent.attribute;
            this.filter = filter;
        }

        /**
         * Filter attributes starting with the specified string.
         * 
         * @param prefix the prefix to filter against. If multiple values are specified any may match
         * @return the filter
         */
        public AttributeFilter starting(CharSequence... prefix) {
            return new AttributeFilter(this, stringFilter(StringOperation.STARTS, prefix));
        }

        /**
         * Filter attributes not starting with the specified string.
         * 
         * @param prefix the prefix to filter against. If multiple values are specified all must match
         * @return the filter
         */
        public AttributeFilter notStarting(CharSequence... prefix) {
            return new AttributeFilter(this, not(stringFilter(StringOperation.STARTS, prefix)));
        }

        /**
         * Filter attributes ending with the specified string.
         * 
         * @param postfix the postfix to filter against. If multiple values are specified any may match
         * @return the filter
         */

        public AttributeFilter ending(CharSequence... postfix) {
            return new AttributeFilter(this, stringFilter(StringOperation.ENDS, postfix));
        }

        /**
         * Filter attributes not ending with the specified string.
         * 
         * @param postfix the postfix to filter against. If multiple values are specified all must match
         * @return the filter
         */
        public AttributeFilter notEnding(CharSequence... postfix) {
            return new AttributeFilter(this, not(stringFilter(StringOperation.ENDS, postfix)));
        }

        /**
         * Filter attributes containing with the specified string.
         * 
         * @param content the contents to filter against. If multiple values are specified any may match
         * @return the filter
         */
        public AttributeFilter containing(CharSequence... content) {
            return new AttributeFilter(this, stringFilter(StringOperation.CONTAINS, content));
        }

        /**
         * Filter attributes not containing with the specified string.
         * 
         * @param content the contents to filter against. If multiple values are specified all must match
         * @return the filter
         */
        public AttributeFilter notContaining(CharSequence... content) {
            return new AttributeFilter(this, not(stringFilter(StringOperation.CONTAINS, content)));
        }

        /**
         * Filter attributes matching the specified string
         * 
         * @param value the values to match
         * @return the filter
         */
        public AttributeFilter matching(CharSequence... value) {
            return new AttributeFilter(this, stringFilter(StringOperation.MATCHES, value));
        }

        /**
         * Filter attributes not matching the specified string
         * 
         * @param value the value to match
         * @return the filter
         */
        public AttributeFilter notMatching(CharSequence... value) {
            return new AttributeFilter(this, not(stringFilter(StringOperation.MATCHES, value)));
        }

        private ResourceFilter stringFilter(StringOperation operation, CharSequence... values) {
            CompoundFilter filter = new CompoundFilter();
            for (CharSequence value : values) {
                filter.add(new StringFilter(this.attribute, operation, value));
            }
            return filter;
        }

        private ResourceFilter not(ResourceFilter filter) {
            return new InvertFilter(filter);
        }

        @Override
        public boolean match(Resource resource) {
            if (this.parent != null && !this.parent.match(resource)) {
                return false;
            }
            return this.filter == null || this.filter.match(resource);
        }
    }

    private static class CompoundFilter implements ResourceFilter {

        private final List<ResourceFilter> filters = new ArrayList<ResourceFilter>();

        public void add(ResourceFilter filter) {
            this.filters.add(filter);
        }

        @Override
        public boolean match(Resource resource) {
            for (ResourceFilter filter : this.filters) {
                if (filter.match(resource)) {
                    return true;
                }
            }
            return false;
        }
    }

    private static class InvertFilter implements ResourceFilter {

        private final ResourceFilter filter;

        public InvertFilter(ResourceFilter filter) {
            this.filter = filter;
        }

        @Override
        public boolean match(Resource resource) {
            return !this.filter.match(resource);
        }
    }

    private enum StringOperation {
        STARTS, ENDS, CONTAINS, MATCHES
    }

    private static class StringFilter implements ResourceFilter {

        private final ResourceAttribute attribute;

        private final StringOperation operation;

        private final CharSequence value;

        public StringFilter(ResourceAttribute attribute, StringOperation operation, CharSequence value) {
            this.attribute = attribute;
            this.operation = operation;
            this.value = value;
        }

        @Override
        public boolean match(Resource resource) {
            String attributeString = this.attribute.get(resource);
            String matchString = this.value.toString();
            if (this.attribute.isIgnoreCase()) {
                attributeString = attributeString.toLowerCase();
                matchString = matchString.toLowerCase();
            }
            switch (this.operation) {
                case STARTS:
                    return attributeString.startsWith(matchString);
                case ENDS:
                    return attributeString.endsWith(matchString);
                case CONTAINS:
                    return attributeString.contains(matchString);
                case MATCHES:
                    return attributeString.equals(matchString);
            }
            return false;
        }
    }
}