package com.sdu.dao.impl;

import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import com.sdu.entity.DataFile;

public class DataFileDaoImpl {
	SessionFactory sessionFactory;
	public DataFileDaoImpl() {
		//System.out.println("DataFileDaoImpl构造函数被调用!");
	}
	public void setSessionFactory(SessionFactory sessionFactory) {
		   this.sessionFactory = sessionFactory;
	}
	
	/*public List<DataFile> getByUserId(int userid){
		Session session = sessionFactory.getCurrentSession();
		Transaction tx = session.beginTransaction();
		//hql没有分号结尾

//		String hql = "from DataFile datafile where datafile.userid = '"+userid+"'";
		List<DataFile> list = null;
		try {
			String hql = "from DataFile datafile ";
			Query query = session.createQuery(hql);
			list = query.list();
			tx.commit();
		} catch (HibernateException e) {
			tx.rollback();
			e.printStackTrace();
		}
		return list;
	}*/
	public List<Object> getByUserId(int userid,int offset,int pageSize){
		Session session = sessionFactory.getCurrentSession();
		Transaction tx = session.beginTransaction();
		//System.out.println("getByUserId=="+userid);
		//hql没有分号结尾
//		String hql = "from DataFile datafile where datafile.userid = '"+userid+"'";
		List<Object> list = null;
		try {
			String sql ="select d.d_id,d.d_name,p.p_name,d.d_type,d.d_size,d.d_createTime from datafile as d,project as p where d.pro_dataid = p.p_id and d.admin_dataid = '"+userid+"' limit "+offset+","+pageSize+";";
//			System.out.println("sql===="+sql);
			Query query = session.createSQLQuery(sql);
			list = query.list();
			tx.commit();
		} catch (HibernateException e) {
			tx.rollback();
			e.printStackTrace();
		}
		return list;
	}
	public int saveDateFile(DataFile dataFile){
	//	System.out.println("进入saveDataFile!");
		Session session = sessionFactory.getCurrentSession();
		Transaction transaction =session.beginTransaction();
		try{
			session.saveOrUpdate(dataFile);
			transaction.commit();
		//	System.out.println("保存成功！");
			//System.out.println("dataFileId:"+dataFile.getD_id());
		}catch(Exception e){
			transaction.rollback();
			e.printStackTrace();
		//	System.out.println("保存失败");
			return 0;
		}
		return dataFile.getD_id();
	}
	public boolean removeById(int d_id){
		Session session = sessionFactory.getCurrentSession();
		Transaction tx = session.beginTransaction();
		try{
			String hql = "delete from DataFile datafile where datafile.d_id = '"+d_id+"'";
		//	System.out.println(hql);
/*			session.createQuery(hql);*/
			session.createQuery(hql).executeUpdate();
			tx.commit();
		}catch(Exception e){
			e.printStackTrace();
			tx.rollback();
			return false;
		}
		return true;
	}
	public boolean updateHashader(int d_id,String hasheader){
		Session session = sessionFactory.getCurrentSession();
		Transaction tx = session.beginTransaction();
		try {
			String hql  = "update DataFile datafile set datafile.d_hasheader = '"+hasheader+"' where datafile.d_id = '"+d_id+"'";
			//System.out.println("hql:"+hql);
			session.createQuery(hql).executeUpdate();
			tx.commit();
		} catch (Exception e) {
			e.printStackTrace();
			tx.rollback();
			return false;
		}
		return true;
	}
	public DataFile getDataFileById(int did){
		//System.out.println("进入dataFile");
		Session session = sessionFactory.getCurrentSession();
	//	System.out.println("1");
		Transaction tx = session.beginTransaction();
		//System.out.println("2");
		//hql没有分号结尾
		List<DataFile> list = null;
		try {
			String hql = "from DataFile datafile where datafile.d_id = '"+did+"'";
			//System.out.println("hql:"+hql);
			Query query = session.createQuery(hql);
			
			list = query.list();
			tx.commit();
		//	System.out.println("查询成功");
		} catch (HibernateException e) {
			tx.rollback();
			e.printStackTrace();
			return null;
		}
		return list.get(0);
	}
	public List<Object> getDataFileByAdminId(int adminId){
		List<Object> list = null;
		Session session = sessionFactory.getCurrentSession();
		Transaction tx = session.beginTransaction();
		try{
			String sql = "select distinct datafile.pro_dataid,datafile.d_type from datafile where datafile.admin_dataid = '"+adminId+"';";
			list = session.createSQLQuery(sql).list();
			tx.commit();
		}catch(Exception e){
			tx.rollback();
			e.printStackTrace();
		}
		return list;
	}
	public List<Object> getDataFilesByProjectId(int adminId, int project_id, int offset, int pageSize) {
		List<Object> list = null;
		Session session = sessionFactory.getCurrentSession();
		Transaction tx = session.beginTransaction();
		try{
			String sql = "select d.d_id,d.d_name,p.p_name,d.d_type,d.d_size,d.d_createTime  from datafile as d,project as p where d.pro_dataid = p.p_id and d.pro_dataid = '"+project_id+"' and d.admin_dataid = '"+adminId+"';";
	//		System.out.println("sql:"+sql);
			list = session.createSQLQuery(sql).list();
			tx.commit();
		}catch(Exception e){
			tx.rollback();
			e.printStackTrace();
		}
		return list;
	}
	public List<Object> getDataFilesByProjectIdAndDataFileType(int projectId,String datafile_type) {
		List<Object> list = null;
		Session session = sessionFactory.getCurrentSession();
		Transaction tx = session.beginTransaction();
		try{
			String sql = "select d.d_id,d.d_name,p.p_name,d.d_type,d.d_size,d.d_createTime  from datafile as d,project as p where p.p_id = d.pro_dataid and d.pro_dataid = '"+projectId+"' and d.d_type='"+datafile_type+"';";
	//		System.out.println("sql:"+sql);
			list = session.createSQLQuery(sql).list();
			tx.commit();
		}catch(Exception e){
			tx.rollback();
			e.printStackTrace();
		}
		return list;
	}
	public boolean removeByIds(String ids) {
		boolean result= false;
		Session session = sessionFactory.getCurrentSession();
		Transaction tx = session.beginTransaction();
		try{
			String sql = "delete from datafile where datafile.d_id in ("+ids+");";
			session.createSQLQuery(sql).executeUpdate();
			tx.commit();
			result = true;
		}catch(Exception e){
			tx.rollback();
			e.printStackTrace();
		}
		return result;
	}
}
