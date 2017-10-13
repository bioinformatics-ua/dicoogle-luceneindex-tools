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
import org.apache.lucene.index.IndexWriter;

import me.tongfei.progressbar.ProgressBar;

/**
 * @author Tiago Marques Godinho, tmgodinho@ua.pt 2017
 *
 */
public class IndexMerger {

	private static CommandLine cli(String[] args) {
		// create Options object
		Options options = new Options();

		// add t option
		Option i = new Option("i", true, "input indexes path - accepts multiple entries");
		i.setRequired(true);
		i.setArgs(Option.UNLIMITED_VALUES);
		options.addOption(i);

		Option o = new Option("o", true, "Output indexes path - preferably an empty folder");
		o.setRequired(true);
		options.addOption(o);

		Option r = new Option("r", true, "Set RAMBufferSize property");
		r.setRequired(false);
		options.addOption(r);

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

		String[] repositories = cmd.getOptionValues("i");
		String targetRep = cmd.getOptionValue("o");

		if (cmd.hasOption("r")) {
			System.setProperty("RAMBufferSize", cmd.getOptionValue("r"));
		}

		IndexWriter wr = null;
		try {
			wr = Utils.openIndexWriter(targetRep);
		} catch (IOException e3) {
			// TODO Auto-generated catch block
			e3.printStackTrace();
			System.exit(-1);
		}

		for (String repository : repositories) {

			System.out.println("Opening Repository: " + repository);

			IndexReader reader = null;
			try {
				reader = Utils.openIndexReader(repository);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			if (reader != null) {
				System.out.println("Number of Documents: " + reader.numDocs());

				ProgressBar pb = new ProgressBar(repository, reader.numDocs());
				pb.start();

				for (int i = 0; i < reader.numDocs(); i++) {

					Document doc = null;
					try {
						doc = reader.document(i);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					if (doc != null) {

						try {
							wr.addDocument(doc);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}

					pb.step();
				}
				pb.stop();

				System.out.println("COMMIT PHASE: THIS WILL TAKE A WHILE");
				try {
					wr.commit();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} else {
				System.out.println("Error opening: " + repository);
			}
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
