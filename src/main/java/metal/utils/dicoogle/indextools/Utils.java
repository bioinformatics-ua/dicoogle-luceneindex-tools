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

import java.io.File;
import java.io.IOException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 * @author Tiago Marques Godinho, tmgodinho@ua.pt 2017
 *
 */
public class Utils {

	public static IndexReader openIndexReader(String repPath) throws IOException{
		
		File fRepository = new File(repPath);		        
		
		IndexReader reader = null;
			//opening reader with directory!!
		Directory dir = FSDirectory.open(fRepository.toPath());
		reader = DirectoryReader.open(dir);
		
		return reader;
	}
	
	public static IndexWriter openIndexWriter(String repPath) throws IOException{
		
		File fRep = new File(repPath);
		
		Directory wrDir = FSDirectory.open(fRep.toPath());
		StandardAnalyzer analyzer = new StandardAnalyzer();		
		
        IndexWriterConfig indexConfig = new IndexWriterConfig(analyzer);
        double maxRAMBufferSize = Double.parseDouble(System.getProperty("RAMBufferSize", "512"));
		indexConfig.setRAMBufferSizeMB(maxRAMBufferSize);
        indexConfig.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
		return new IndexWriter(wrDir, indexConfig);
	}
	
}
