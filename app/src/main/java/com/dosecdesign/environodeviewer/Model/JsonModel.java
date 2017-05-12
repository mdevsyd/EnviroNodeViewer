package com.dosecdesign.environodeviewer.Model;

import java.util.List;

/**
 * Created by Michi on 5/05/2017.
 */

public class JsonModel {


    /**
     * success : true
     * message :
     * data : {"account_sid":"A3JHBLG1ZBYLK63Q","sites":[{"name":"53","hubs":[{"serial":"F000016C","name":null,"instruments":[{"serial":"AML1G204","name":null},{"serial":"WLM1H101","name":null},{"serial":"WLM1H103","name":null},{"serial":"F000016C","name":null},{"serial":"WLM1H102","name":null}]},{"serial":"F00000F9","name":null,"instruments":[{"serial":"SFM1F90J","name":"Gum Tree - Newholme"},{"serial":"AML1E901","name":"ICT AWS"}]},{"serial":"F000014B","name":null,"instruments":[{"serial":"SFM1F90O","name":"Stringy bark UNE Smart Farm"},{"serial":"AML1G302","name":"ICT AWS"},{"serial":"D0000001","name":null}]}]}]}
     */

    private boolean success;
    private String message;
    private DataBean data;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * account_sid : A3JHBLG1ZBYLK63Q
         * sites : [{"name":"53","hubs":[{"serial":"F000016C","name":null,"instruments":[{"serial":"AML1G204","name":null},{"serial":"WLM1H101","name":null},{"serial":"WLM1H103","name":null},{"serial":"F000016C","name":null},{"serial":"WLM1H102","name":null}]},{"serial":"F00000F9","name":null,"instruments":[{"serial":"SFM1F90J","name":"Gum Tree - Newholme"},{"serial":"AML1E901","name":"ICT AWS"}]},{"serial":"F000014B","name":null,"instruments":[{"serial":"SFM1F90O","name":"Stringy bark UNE Smart Farm"},{"serial":"AML1G302","name":"ICT AWS"},{"serial":"D0000001","name":null}]}]}]
         */

        private String account_sid;
        private List<SitesBean> sites;

        public String getAccount_sid() {
            return account_sid;
        }

        public void setAccount_sid(String account_sid) {
            this.account_sid = account_sid;
        }

        public List<SitesBean> getSites() {
            return sites;
        }

        public void setSites(List<SitesBean> sites) {
            this.sites = sites;
        }

        public static class SitesBean {
            /**
             * name : 53
             * hubs : [{"serial":"F000016C","name":null,"instruments":[{"serial":"AML1G204","name":null},{"serial":"WLM1H101","name":null},{"serial":"WLM1H103","name":null},{"serial":"F000016C","name":null},{"serial":"WLM1H102","name":null}]},{"serial":"F00000F9","name":null,"instruments":[{"serial":"SFM1F90J","name":"Gum Tree - Newholme"},{"serial":"AML1E901","name":"ICT AWS"}]},{"serial":"F000014B","name":null,"instruments":[{"serial":"SFM1F90O","name":"Stringy bark UNE Smart Farm"},{"serial":"AML1G302","name":"ICT AWS"},{"serial":"D0000001","name":null}]}]
             */

            private String name;
            private List<HubsBean> hubs;

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public List<HubsBean> getHubs() {
                return hubs;
            }

            public void setHubs(List<HubsBean> hubs) {
                this.hubs = hubs;
            }

            public static class HubsBean {
                /**
                 * serial : F000016C
                 * name : null
                 * instruments : [{"serial":"AML1G204","name":null},{"serial":"WLM1H101","name":null},{"serial":"WLM1H103","name":null},{"serial":"F000016C","name":null},{"serial":"WLM1H102","name":null}]
                 */

                private String serial;
                private Object name;
                private List<InstrumentsBean> instruments;

                public String getSerial() {
                    return serial;
                }

                public void setSerial(String serial) {
                    this.serial = serial;
                }

                public Object getName() {
                    return name;
                }

                public void setName(Object name) {
                    this.name = name;
                }

                public List<InstrumentsBean> getInstruments() {
                    return instruments;
                }

                public void setInstruments(List<InstrumentsBean> instruments) {
                    this.instruments = instruments;
                }

                public static class InstrumentsBean {
                    /**
                     * serial : AML1G204
                     * name : null
                     */

                    private String serial;
                    private Object name;

                    public String getSerial() {
                        return serial;
                    }

                    public void setSerial(String serial) {
                        this.serial = serial;
                    }

                    public Object getName() {
                        return name;
                    }

                    public void setName(Object name) {
                        this.name = name;
                    }
                }
            }
        }
    }
}
