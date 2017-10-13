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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;

/**
 * @author Tiago Marques Godinho, tmgodinho@ua.pt 2017
 *
 */
public class IndexRepair {

	private static CommandLine cli(String[] args) {
		// create Options object
		Options options = new Options();

		// add t option
		Option i = new Option("i", true, "repository path");
		i.setRequired(true);
		options.addOption(i);

		Option o = new Option("o", true, "annonimized output repository path");
		o.setRequired(true);
		options.addOption(o);

		Option l = new Option("l", true, "List of tags to repair");
		l.setRequired(false);
		l.setArgs(5);
		options.addOption(l);

		Option v = new Option("v", true, "List of default values, same order as tags.");
		v.setRequired(false);
		v.setArgs(5);
		options.addOption(v);

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

	public static void main(String args[]) {
		CommandLine cmd = cli(args);

		String repository = cmd.getOptionValue("i");
		String targetRep = cmd.getOptionValue("o");
		String[] labels = cmd.getOptionValues("l");
		String[] values = cmd.getOptionValues("v");
		if (labels == null) {
			labels = new String[] { "uri", "FileSize" };
			values = new String[] { "err:///no-uri/", "-1" };
		}

		HashMap<String, String> tagsMap = new HashMap<String, String>(labels.length);
		for (int i = 0; i < labels.length; i++) {
			String l = labels[i];
			String v = (values.length < i) ? values[i] : "";
			tagsMap.put(l, v);
		}

		IndexReader reader = null;
		IndexWriter wr = null;
		try {
			reader = Utils.openIndexReader(repository);
			wr = Utils.openIndexWriter(targetRep);
		} catch (IOException e3) {
			// TODO Auto-generated catch block
			e3.printStackTrace();
			System.exit(-1);
		}

		System.out.println("Number of Documents: " + reader.numDocs());

		for (int i = 0; i < reader.numDocs(); i++) {
			Document doc = null;
			try {
				doc = reader.document(i);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (doc != null) {

				for (Entry<String, String> entry : tagsMap.entrySet()) {
					Field f = (Field) doc.getField(entry.getKey());

					if (f == null) {
						f = new StringField(entry.getKey(), entry.getValue(), Field.Store.YES);

						doc.add(f);

						System.out.printf("Document: %d, Added Fild: %s\n", i, f);
					}
				}

				try {
					wr.addDocument(doc);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		System.out.println("COMMIT PHASE: THIS WILL TAKE A WHILE");
		try {
			wr.commit();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("OPTIMIZE PHASE: THIS WILL TAKE A WHILE");

		try {
			wr.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
