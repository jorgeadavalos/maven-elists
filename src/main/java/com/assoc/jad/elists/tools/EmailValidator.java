package com.assoc.jad.elists.tools;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

public class EmailValidator implements Validator {

	private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-]+(\\." +
			"[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*" +
			"(\\.[A-Za-z]{2,})$";

	private Pattern pattern;
	private Matcher matcher;

	public void validate(FacesContext context, UIComponent component,
			Object object) throws ValidatorException {
		
		pattern = Pattern.compile(EMAIL_PATTERN);
		FacesMessage msg = new FacesMessage();
		if (object instanceof String) {
			String email = (String)object;
			String msgResp = checkEmail(email);
			if (msgResp.length()>0) {
				msg.setSummary(msgResp);
				msg.setDetail(msgResp);
				msg.setSeverity(FacesMessage.SEVERITY_ERROR);
				throw new ValidatorException(msg);
			}
		}
	}
	public String checkEmail(String allEmails) {
		String[] emails = allEmails.split(System.lineSeparator());
		String msg = "";
		for (int i =0;i<emails.length;i++ ) {
			matcher = pattern.matcher(emails[i]);
			if(!matcher.matches())
				msg += "E-mail validation failed." + emails[i]+" Invalid E-mail format.";
		}
		return msg;
	}
}
