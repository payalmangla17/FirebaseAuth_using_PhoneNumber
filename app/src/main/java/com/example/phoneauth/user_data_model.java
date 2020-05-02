package com.example.phoneauth;

public class user_data_model {

    public String mobile_no,UID;

    public user_data_model(String mobile_no,String UID) {

        this.mobile_no = mobile_no;
        this.UID=UID;


    }

    @Override
    public String toString() {
        return "helper_user{" +

               " mobile_no='" + mobile_no + '\''+
                " uid='" + UID + '\''+
                '}';
    }
}
