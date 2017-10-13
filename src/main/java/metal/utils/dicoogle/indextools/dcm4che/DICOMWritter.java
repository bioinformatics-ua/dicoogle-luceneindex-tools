package metal.utils.dicoogle.indextools.dcm4che;

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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.index.IndexableField;
import org.dcm4che3.data.Attributes;
import org.dcm4che3.data.ElementDictionary;
import org.dcm4che3.data.VR;
import org.dcm4che3.util.TagUtils;

public class DICOMWritter {

	private static final Logger logger = LogManager.getLogger(DICOMWritter.class);

	private ElementDictionary standard_dict = ElementDictionary.getStandardElementDictionary();

	private Attributes fmi;
	private Attributes attrs;

	public DICOMWritter() {
		super();
		this.fmi = null;
		this.attrs = new Attributes();
	}

	public void parseFieldList(List<IndexableField> fields) {

		fields = new ArrayList<IndexableField>(fields);

		sortFields(fields);

		for (IndexableField f : fields) {

			String name = f.name();
			if (name.contains("_"))
				name = "SQ";
			switch (name) {
			case "FileSize":
			case "uri":
			case "others":
			case "SQ":
				break;
			default:
				doReadDataset(f);
			}
		}
	}

	public Attributes getFmi() {
		return fmi;
	}

	public Attributes getAttrs() {
		return attrs;
	}

	private static void sortFields(List<IndexableField> fields) {

		fields.sort(new Comparator<IndexableField>() {

			@Override
			public int compare(IndexableField o1, IndexableField o2) {
				String tagName = o1.name();
				int tag1 = TagUtils.forName(tagName);

				tagName = o2.name();
				int tag2 = TagUtils.forName(tagName);

				return tag1 - tag2;
			}

		});

	}

	private void doReadDataset(IndexableField field) {
		String value = field.stringValue();
		String tagName = field.name();
		
		if(value == null || value.length() == 0){
			logger.warn("Empy Field: {} - {} - {} - {} ### Replacing", tagName, value);
			return;
		}
		
		int tag = TagUtils.forName(tagName);

		if (TagUtils.isFileMetaInformation(tag)) {
			if (fmi == null)
				fmi = new Attributes();
			attrs = fmi;
		}

		VR vr = standard_dict.vrOf(tag);

		if (attrs.contains(tag)) {
			logger.warn("Already has Tag: {} - {} - {} - {} ### Replacing", tagName, tag, vr, field.stringValue());
		}
		
		switch (vr) {
		case AT:
			logger.debug("trying to add: {} - {} - {} - {}", tagName, tag, vr, field.stringValue().getBytes());
			attrs.setBytes(tag, vr, field.stringValue().getBytes());
			logger.info("Added: {} - {} - {} - {}", tagName, tag, vr, field.stringValue().getBytes());
			break;
		case PN:
					
		case AE:
		case AS:
		case CS:
		case DA:
		case DT:
		case LO:
		case LT:
		case SH:
		case ST:
		case TM:
		case UC:
		case UI:
		case UR:
		case UT:
			logger.debug("trying to add: {} - {} - {} - {}", tagName, tag, vr, field.stringValue());
			attrs.setString(tag, vr, field.stringValue());
			logger.info("Added: {} - {} - {} - {}", tagName, tag, vr, field.stringValue());
			break;
		case DS:
		case FL:
		case FD:
			logger.debug("trying to add: {} - {} - {} - {}", tagName, tag, vr, field.stringValue());
			Double d = Double.parseDouble(field.stringValue());
			attrs.setDouble(tag, vr, d);
			logger.info("Added: {} - {} - {} - {}", tagName, tag, vr, d);
			break;
		case IS:
		case SL:
		case SS:
		case UL:
		case US:
			logger.debug("trying to add: {} - {} - {} - {}", tagName, tag, vr, field.stringValue());
			Double d2 = Double.parseDouble(field.stringValue());
			int i = d2.intValue();
			attrs.setInt(tag, vr, i);
			logger.info("Added: {} - {} - {} - {}", tagName, tag, vr, i);
			break;
		case SQ:
			// readSequence(attrs, tag);
			break;
		case OB:
		case OD:
		case OF:
		case OW:
			logger.error("Should not have reached it: {} {} {}", tagName, tag, vr);
			break;
		case UN:
			logger.error("Unknow TAG: {} {} {}", tagName, tag, vr);
			break;
		default:
			logger.error("What da fuck: {} {} {}", tagName, tag, vr);

		}

	}
}
