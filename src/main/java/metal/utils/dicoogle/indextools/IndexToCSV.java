package metal.utils.dicoogle.indextools;

/*-
 * #%L
 * Dicoogle-LuceneIndex-Tools
 * %%
 * Copyright (C) 2017 Tiago Marques Godinho
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexableField;

/**
 * @author Tiago Marques Godinho, tmgodinho@ua.pt 2017
 *
 */
public class IndexToCSV {

	private static CommandLine cli(String[] args) {
		// create Options object
		Options options = new Options();

		// add t option
		Option i = new Option("i", true, "repository path");
		i.setRequired(true);
		options.addOption(i);

		Option o = new Option("o", true, "Output file path");
		o.setRequired(true);
		options.addOption(o);

		Option l = new Option("l", true, "List of Labels to export");
		l.setRequired(false);
		l.setArgs(5);
		options.addOption(l);

		CommandLineParser parser = new PosixParser();
		CommandLine cmd = null;
		try {
			cmd = parser.parse(options, args);
		} catch (ParseException e) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp("ant", options);
			System.exit(-1);
		}
		return cmd;
	}

	public static void main(String args[]) throws IOException {
		CommandLine cmd = cli(args);

		String repPath = cmd.getOptionValue("i");
		String[] labels = cmd.getOptionValues("l");
		String outFile = cmd.getOptionValue("o");
		IndexReader reader = null;
		try {
			reader = Utils.openIndexReader(repPath);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		if (labels.length == 0) {
		//	labels = (String[]) reader.getFieldNames(FieldOption.STORES_PAYLOADS).toArray();
		}
		System.out.println("Number of Documents: " + reader.numDocs());

		final Appendable out = new BufferedWriter(new FileWriter(outFile));
		final CSVPrinter printer = CSVFormat.DEFAULT.withHeader(labels).print(out);

		for (int i = 0; i < reader.numDocs(); i++) {
			Document doc = null;
			try {
				doc = reader.document(i);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (doc != null) {
				List<String> results = new ArrayList<>(labels.length);
				for (String l : labels) {
					IndexableField value = doc.getField(l);
					results.add(StringUtils.trimToEmpty(value.stringValue()));
				}
				printer.printRecord(results);

			}

		}
	}

}
