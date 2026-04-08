package com.example.socialnetworkingbackend.constant;

public enum SortByDataConstant implements SortByInterface {

    USER {
        @Override
        public String getSortBy(String sortBy) {
            switch (sortBy) {
                case "firstName":
                    return "first_name";
                case "lastName":
                    return "last_name";
                case "lastModifiedAt":
                    return "last_modified_at";
                default:
                    return "created_at";
            }
        }
    },

    MEDIA {
        @Override
        public String getSortBy(String sortBy) {
            switch (sortBy) {
                case "data_size":
                    return "dataSize";
                default:
                    return "createdAt";
            }
        }
    },

    POST {
        @Override
        public String getSortBy(String sortBy) {
            switch (sortBy) {
                case "title":
                    return "title";
                case "lastModifiedAt":
                    return "lastModifiedAt";
                default:
                    return "createdAt";
            }
        }
    }

}