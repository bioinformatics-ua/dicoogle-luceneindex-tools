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

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;

/**
 * @author Tiago Marques Godinho, tmgodinho@ua.pt 2017
 *
 */
public class IndexAnalizer {

	private static CommandLine cli(String[] args) {
		// create Options object
		Options options = new Options();

		// add t option
		Option i = new Option("i", true, "repository path");
		i.setRequired(true);
		options.addOption(i);

		Option l = new Option("l", true, "List of Labels to print");
		l.setRequired(true);
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

	public static void main(String args[]) {
		CommandLine cmd = cli(args);

		String repPath = cmd.getOptionValue("i");
		String[] labels = cmd.getOptionValues("l");
		IndexReader reader = null;
		try {
			reader = Utils.openIndexReader(repPath);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
		System.out.println("Number of Documents: "+reader.numDocs());

		for (int i = 0; i < reader.numDocs(); i++) {
			
			Document doc = null;
			try {
				doc = reader.document(i);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (doc != null) {
				for (String l : labels) {
					System.out.print(doc.getField(l));
				}
				System.out.println("");
			}			
		}
	}

}
