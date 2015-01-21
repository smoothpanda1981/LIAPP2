package com.yan.wang.liapp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yan.wang.liapp.dao.Customer;
import com.yan.wang.liapp.dao.CustomerVoucher;
import com.yan.wang.liapp.dao.Voucher;
import com.yan.wang.liapp.dao.VoucherToReturn;


/**
 * Handles requests for the application home page.
 */
@Controller
public class HomeController {
	
	private static final Logger logger = LoggerFactory.getLogger(HomeController.class);
	
	/**
	 * Simply selects the home view to render by returning its name.
	 */
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String home(Locale locale, Model model) {
		logger.info("Welcome home! The client locale is {}.", locale);
		
		Date date = new Date();
		DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, locale);
		
		String formattedDate = dateFormat.format(date);
		
		model.addAttribute("serverTime", formattedDate );
		model.addAttribute("message", "Hello Doug !");
		
		return "home";
	}
	
	@RequestMapping(value = "db", method = RequestMethod.GET)
	public String db(ModelMap model) {
		model.addAttribute("message", "DB Status");

		try {
			Class.forName("org.hsqldb.jdbc.JDBCDriver" );
			Connection c = DriverManager.getConnection("jdbc:hsqldb:hsql://localhost", "SA", "");
			System.out.println("Schema = " + c.getSchema());

			Statement statement = c.createStatement();
			ResultSet resultSet;

			resultSet = statement.executeQuery("select * from customer");
			ResultSetMetaData meta   = resultSet.getMetaData();
			/*
			String[] columnsNames = new String[meta.getColumnCount()];
			System.out.println(meta.getColumnCount());
			for (int i = 1; i <= columnsNames.length; i++) {
				columnsNames[i-1] = meta.getColumnName(i);
				System.out.println(columnsNames[i-1]);
			}
			*/
			List<Customer> customerList = new ArrayList<Customer>();
			while (resultSet.next()) {
				Customer customer = new Customer();
				customer.setCustomer_id(resultSet.getInt(meta.getColumnName(1)));
				customer.setEmail(resultSet.getString(meta.getColumnName(2)));
				customerList.add(customer);
			}
			model.addAttribute("customerList", customerList);


			resultSet = statement.executeQuery("select * from voucher");
			meta   = resultSet.getMetaData();
			List<Voucher> voucherList = new ArrayList<Voucher>();
			while (resultSet.next()) {
				Voucher voucher = new Voucher();
				voucher.setVoucher_id(resultSet.getInt(meta.getColumnName(1)));
				voucher.setVoucher_text(resultSet.getString(meta.getColumnName(2)));
				voucher.setCompany_name(resultSet.getString(meta.getColumnName(3)));
				voucher.setFlag(resultSet.getInt(meta.getColumnName(4)));
				voucherList.add(voucher);
			}
			model.addAttribute("voucherList", voucherList);


			resultSet = statement.executeQuery("select * from customer_voucher");
			meta   = resultSet.getMetaData();
			List<CustomerVoucher> customerVoucherList = new ArrayList<CustomerVoucher>();
			while (resultSet.next()) {
				CustomerVoucher customerVoucher = new CustomerVoucher();
				customerVoucher.setId(resultSet.getInt(meta.getColumnName(1)));
				customerVoucher.setCustomer_id(resultSet.getInt(meta.getColumnName(2)));
				customerVoucher.setVoucher_id(resultSet.getInt(meta.getColumnName(3)));
				customerVoucherList.add(customerVoucher);
			}
			model.addAttribute("customerVoucherList", customerVoucherList);

		} catch (Exception e) {
			System.err.println("ERROR: failed to load HSQLDB JDBC driver.");
			e.printStackTrace();
		}
		return "DB_Status";
	}


	@RequestMapping(value = "tab", params = {"email", "company"}, method = {RequestMethod.POST, RequestMethod.GET})
	public @ResponseBody VoucherToReturn tab(@RequestParam(value = "email") String email, @RequestParam(value = "company") String company_name) {
		System.out.println("In Tab");
		VoucherToReturn voucherToReturn = new VoucherToReturn();

		int newCustomerId = 0;
		int newCVId = 0;
		String voucherText = "";

		try {
			Class.forName("org.hsqldb.jdbc.JDBCDriver" );
			Connection c = DriverManager.getConnection("jdbc:hsqldb:hsql://localhost", "SA", "");
			System.out.println("Schema = " + c.getSchema());

			Statement statement = c.createStatement();
			ResultSet resultSet;

			resultSet = statement.executeQuery("select max(customer_id) from customer");
			ResultSetMetaData meta   = resultSet.getMetaData();

			while (resultSet.next()) {
				int maxID = resultSet.getInt(meta.getColumnName(1));
				int newID = maxID + 1;
				System.out.println(maxID + 1);
				resultSet = statement.executeQuery("insert into customer values ('" + newID + "' , '" + email + "')");
			}

			resultSet = statement.executeQuery("select max(customer_id) from customer");
			while (resultSet.next()) {
				newCustomerId = resultSet.getInt(meta.getColumnName(1));
			}

			resultSet = statement.executeQuery("select min(voucher_id) from voucher where flag = '0' and company_name = '"+ company_name +"'");
			while (resultSet.next()) {
				int minID = resultSet.getInt(meta.getColumnName(1));
				resultSet = statement.executeQuery("select voucher_text from voucher where voucher_id = '"+ minID  +"'");
				while (resultSet.next()) {
					voucherText = resultSet.getString("voucher_text");
					System.out.println(voucherText);
				}

				statement.executeQuery("update voucher set flag = '1' where voucher_id = '" + minID  +"'");


				resultSet = statement.executeQuery("select max(id) from customer_voucher");
				meta   = resultSet.getMetaData();

				int cvMaxID = 0;
				while (resultSet.next()) {
					cvMaxID = resultSet.getInt(meta.getColumnName(1));
					newCVId = cvMaxID + 1;
					System.out.println(newCVId);
				}

				statement.executeQuery("insert into customer_voucher values('"+newCVId+"', '"+newCustomerId+"', '"+minID+"')");
			}

			voucherToReturn.setEmail(email);
			voucherToReturn.setCompanyName(company_name);
			voucherToReturn.setVoucherText(voucherText);

		} catch (Exception e) {
			System.err.println("ERROR: failed to load HSQLDB JDBC driver.");
			e.printStackTrace();
		}
		return voucherToReturn;
	}
	
}
