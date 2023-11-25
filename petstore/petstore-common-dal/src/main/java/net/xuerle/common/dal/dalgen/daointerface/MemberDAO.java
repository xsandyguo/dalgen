/*
 * petstore.net Inc.
 * Copyright (c) 2020 - 2023 All Rights Reserved.
 * Powered By [dalgen]
 */
package net.petstore.common.dal.dalgen.daointerface;
import org.springframework.dao.DataAccessException;
import net.petstore.common.dal.dalgen.operation.member.*;
import net.petstore.common.dal.dalgen.dataobject.*;


import java.io.*;
import java.net.*;
import java.util.*;

import java.math.BigDecimal;
import java.math.BigInteger;

import net.petstore.common.dal.utils.PageQueryUtils;
import net.petstore.common.dal.utils.PageQuery;
import net.petstore.common.dal.utils.Paginator;
import net.petstore.common.dal.utils.PageList;



/**
 * MemberDAO
 * database table: member
 * database table comments: Member
 * This file is generated by <tt>dalgen</tt>, a DAL (Data Access Layer)
 * 
 * PLEASE DO NOT MODIFY THIS FILE MANUALLY, or else your modification may
 * be OVERWRITTEN by someone else. To modify the file, you should go to 
 * directory <tt>(project-home)/dalgen</tt>
 * @author dalgen
 *
 
 */
public interface MemberDAO {


	/**
	 * 
	 * sql:
	 * <pre>INSERT      INTO         member         (           id ,uuid ,real_name ,password ,phone_number ,age ,qq_number ,signup_date ,wechat_uid ,nick_name           )      VALUES         (?,?,?,?,?,?,?,?,?,?)</pre> 
	 */
	Integer insert(MemberDO member) throws DataAccessException;

	/**
	 * 
	 * sql:
	 * <pre>DELETE      FROM         member      WHERE         id = ?</pre> 
	 */
	int delete(Integer id) throws DataAccessException;

	/**
	 * 
	 * sql:
	 * <pre>UPDATE         member      SET         uuid = ? ,real_name = ? ,password = ? ,phone_number = ? ,age = ? ,qq_number = ? ,signup_date = ? ,wechat_uid = ? ,nick_name = ?                WHERE         id = ?</pre> 
	 */
	int update(MemberDO member) throws DataAccessException;

	/**
	 * 
	 * sql:
	 * <pre>UPDATE         member          set         uuid = ? ,                real_name = ? ,                password = ? ,                phone_number = ? ,                age = ? ,                qq_number = ? ,                signup_date = ? ,                wechat_uid = ? ,                nick_name = ?                    where         id = ?</pre> 
	 */
	int updateIf(MemberDO member) throws DataAccessException;

	/**
	 * 
	 * sql:
	 * <pre>SELECT         id, uuid, real_name, password, phone_number, age, qq_number, signup_date, wechat_uid, nick_name                  FROM         member               WHERE         id = ?</pre> 
	 */
	MemberDO queryById(Integer id) throws DataAccessException;

	/**
	 * 
	 * sql:
	 * <pre>SELECT         id, uuid, real_name, password, phone_number, age, qq_number, signup_date, wechat_uid, nick_name                  FROM         member      where         uuid = ?</pre> 
	 */
	MemberDO queryByUuid(String uuid) throws DataAccessException;

	/**
	 * 
	 * sql:
	 * <pre>SELECT         id, uuid, real_name, password, phone_number, age, qq_number, signup_date, wechat_uid, nick_name            FROM         member          WHERE         uuid = ?                         AND real_name = ?                         AND password = ?                         AND phone_number = ?                         AND age = ?                         AND qq_number = ?                         AND signup_date = ?                         AND wechat_uid = ?                         AND nick_name = ?</pre> 
	 */
	PageList<MemberDO> findPage(FindPageQuery param) throws DataAccessException;

}



