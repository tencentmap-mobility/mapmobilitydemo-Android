package com.map.mapmobility.carpreview.helper;

import com.map.mapmobility.carpreview.CarPreviewTaskActivity;
import com.map.mapmobility.utils.StringUtils;

import java.util.ArrayList;

public class Helper {

    public static ArrayList<String> carTypeFactory(int type) {
        ArrayList types = new ArrayList();
        switch (type){
            case CarPreviewTaskActivity.TAXI_TYPE:
                types.add(StringUtils.toStr(CarPreviewTaskActivity.TAXI_TYPE));
                break;
            case CarPreviewTaskActivity.ENERGY_TYPE:
                types.add(StringUtils.toStr(CarPreviewTaskActivity.ENERGY_TYPE));
                break;
            case CarPreviewTaskActivity.COMFORTABLE_TYPE:
                types.add(StringUtils.toStr(CarPreviewTaskActivity.COMFORTABLE_TYPE));
                break;
            case CarPreviewTaskActivity.LUXURY_TYPE:
                types.add(StringUtils.toStr(CarPreviewTaskActivity.LUXURY_TYPE));
                break;
            case CarPreviewTaskActivity.BUSINESS_TYPE:
                types.add(StringUtils.toStr(CarPreviewTaskActivity.BUSINESS_TYPE));
                break;
            case CarPreviewTaskActivity.ECONOMIC_TYPE:
                types.add(StringUtils.toStr(CarPreviewTaskActivity.ECONOMIC_TYPE));
                break;
            case CarPreviewTaskActivity.MORE_CAR:
                types.add(StringUtils.toStr(CarPreviewTaskActivity.TAXI_TYPE));
                types.add(StringUtils.toStr(CarPreviewTaskActivity.ENERGY_TYPE));
                types.add(StringUtils.toStr(CarPreviewTaskActivity.COMFORTABLE_TYPE));
                types.add(StringUtils.toStr(CarPreviewTaskActivity.LUXURY_TYPE));
                types.add(StringUtils.toStr(CarPreviewTaskActivity.BUSINESS_TYPE));
                types.add(StringUtils.toStr(CarPreviewTaskActivity.ECONOMIC_TYPE));
                break;
        }
        return types;
    }

}
