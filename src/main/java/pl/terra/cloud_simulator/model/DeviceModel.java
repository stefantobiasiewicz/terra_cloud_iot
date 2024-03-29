package pl.terra.cloud_simulator.model;

import pl.terra.common.mqtt.DeviceMqtt;
import pl.terra.device.model.EnvInfo;
import pl.terra.device.model.Heater;
import pl.terra.device.model.StatusResp;

import java.io.Serializable;

public class DeviceModel implements Serializable {
    private final String deviceCode;
    private DeviceMqtt deviceMqtt;
    private boolean lightOnOff;
    private boolean doorsOpenClose;
    private boolean heaterOnOff;
    private double heaterSetTemp;
    private boolean fanOnOff;
    private double temperature;
    private double pressure;
    private double humidity;
    private boolean humidifierOnOff;

    public DeviceModel() {
        deviceCode = "should not be ever used";
    }

    public DeviceModel(final String deviceCode) {
        this.deviceCode = deviceCode;
        lightOnOff = false;
        doorsOpenClose = false;
        heaterOnOff = false;
        heaterSetTemp = 22.2f;
        fanOnOff = false;
        temperature = 23.3;
        pressure = 980;
        humidity = 50;
        humidifierOnOff = false;
    }

    public String getDeviceCode() {
        return deviceCode;
    }

    public DeviceMqtt getDeviceMqtt() {
        return deviceMqtt;
    }

    public void setDeviceMqtt(DeviceMqtt deviceMqtt) {
        this.deviceMqtt = deviceMqtt;
    }

    public boolean isLightOnOff() {
        return lightOnOff;
    }

    public void setLightOnOff(boolean lightOnOff) {
        this.lightOnOff = lightOnOff;
    }

    public boolean isDoorsOpenClose() {
        return doorsOpenClose;
    }

    public void setDoorsOpenClose(boolean doorsOpenClose) {
        this.doorsOpenClose = doorsOpenClose;
    }

    public boolean isHeaterOnOff() {
        return heaterOnOff;
    }

    public void setHeaterOnOff(boolean heaterOnOff) {
        this.heaterOnOff = heaterOnOff;
    }

    public double getHeaterSetTemp() {
        return heaterSetTemp;
    }

    public void setHeaterSetTemp(double heaterSetTemp) {
        this.heaterSetTemp = heaterSetTemp;
    }

    public boolean isFanOnOff() {
        return fanOnOff;
    }

    public void setFanOnOff(boolean fanOnOff) {
        this.fanOnOff = fanOnOff;
    }

    public double getTemperature() {
        return temperature;
    }

    public double getTemperature(final Double noise) {
        temperature = temperature + noise;
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public double getPressure() {
        return pressure;
    }

    public double getPressure(double noise) {
        pressure = pressure + noise;
        return pressure;
    }

    public void setPressure(double pressure) {
        this.pressure = pressure;
    }

    public double getHumidity() {
        return humidity;
    }
    public double getHumidity(final Double noise) {
        humidity = humidity + noise;
        return humidity;
    }

    public void setHumidity(double humidity) {
        this.humidity = humidity;
    }

    public boolean isHumidifierOnOff() {
        return humidifierOnOff;
    }

    public void setHumidifierOnOff(boolean humidifierOnOff) {
        this.humidifierOnOff = humidifierOnOff;
    }

    public StatusResp asStatusResp() {
        final StatusResp status = new StatusResp();
        status.light(lightOnOff);
        status.setDoors(doorsOpenClose);
        final Heater heater = new Heater();
        heater.setOnOff(heaterOnOff);
        heater.setTemp(heaterSetTemp);
        status.setHeater(heater);
        status.setFan(fanOnOff);
        final EnvInfo envInfo = new EnvInfo();
        envInfo.setTemperature(temperature);
        envInfo.setPressure(pressure);
        envInfo.setHumidity(humidity);
        status.setEnvInfo(envInfo);
        status.setHumidifier(humidifierOnOff);

        return status;
    }
}
