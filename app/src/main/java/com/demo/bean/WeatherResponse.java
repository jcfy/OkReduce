package com.demo.bean;

public class WeatherResponse<T> {
    private String success;

    private T result;

    public void setSuccess(String success){
        this.success = success;
    }
    public String getSuccess(){
        return this.success;
    }
    public void setResult(T result){
        this.result = result;
    }
    public T getResult(){
        return this.result;
    }

    @Override
    public String toString() {
        return "WeatherResponse{" +
                "success='" + success + '\'' +
                ", result=" + result +
                '}';
    }
}
