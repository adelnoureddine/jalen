/*
 * Copyright (c) 2014, Inria, University Lille 1.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Affero General Public License v3.0
 * which accompanies this distribution, and is available at
 * https://www.gnu.org/licenses/agpl-3.0.html
 *
 * Author : Adel Noureddine
 */

package jalen;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class JalenFormatter extends Formatter {
	private final DateFormat df = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss.SSS");

	@Override
	public String format(LogRecord record) {
		StringBuilder builder = new StringBuilder(1000);
		builder.append(df.format(new Date(record.getMillis()))).append(" - ");
		//builder.append("[").append(record.getSourceClassName()).append(".");
		//builder.append(record.getSourceMethodName()).append("] - ");
		builder.append("[").append(record.getLevel()).append("] - ");
		builder.append(formatMessage(record));
		builder.append("\n");
		return builder.toString();
	}
}
