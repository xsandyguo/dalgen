package com.petstore.application;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.petstore.common.dal.dalgen.daointerface.MemberDAO;

@Component
public class MyTestBean implements InitializingBean {
  @Autowired
  private MemberDAO memberDAO;

  @Override
  public void afterPropertiesSet() throws Exception {
    System.out.println(memberDAO.queryById(122));
  }
}
