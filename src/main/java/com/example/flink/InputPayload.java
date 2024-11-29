package com.example.flink;

import java.util.Objects;

public class InputPayload {

  private String name;
  private String address;
  private String dateOfBirth;
  private int age;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public String getDateOfBirth() {
    return dateOfBirth;
  }

  public void setDateOfBirth(String dateOfBirth) {
    this.dateOfBirth = dateOfBirth;
  }

  public int getAge() {
    return age;
  }

  public void setAge(int age) {
    this.age = age;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    InputPayload inputPayload = (InputPayload) o;
    return age == inputPayload.age
        && Objects.equals(name, inputPayload.name)
        && Objects.equals(address, inputPayload.address)
        && Objects.equals(dateOfBirth, inputPayload.dateOfBirth);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, address, dateOfBirth, age);
  }
}
