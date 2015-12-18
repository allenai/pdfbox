/*****************************************************************************
 * 
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * 
 ****************************************************************************/

package org.allenai.pdfbox.preflight.annotation.pdfa;

import org.allenai.pdfbox.preflight.PreflightConstants;
import org.allenai.pdfbox.preflight.annotation.AnnotationValidatorFactory;
import org.allenai.pdfbox.preflight.annotation.LineAnnotationValidator;
import org.allenai.pdfbox.preflight.annotation.LinkAnnotationValidator;
import org.allenai.pdfbox.preflight.annotation.MarkupAnnotationValidator;
import org.allenai.pdfbox.preflight.annotation.PopupAnnotationValidator;
import org.allenai.pdfbox.preflight.annotation.SquareCircleAnnotationValidator;
import org.allenai.pdfbox.preflight.annotation.TextAnnotationValidator;
import org.allenai.pdfbox.preflight.annotation.WidgetAnnotationValidator;
import org.allenai.pdfbox.preflight.annotation.FreeTextAnnotationValidator;
import org.allenai.pdfbox.preflight.annotation.InkAnnotationValidator;
import org.allenai.pdfbox.preflight.annotation.PrintMarkAnnotationValidator;
import org.allenai.pdfbox.preflight.annotation.RubberStampAnnotationValidator;
import org.allenai.pdfbox.preflight.annotation.TrapNetAnnotationValidator;

/**
 * Factory to instantiate AnnotationValidator for a PDF/A-1b validation.
 */
public class PDFAbAnnotationFactory extends AnnotationValidatorFactory
{

    @Override
    protected void initializeClasses()
    {
        this.validatorClasses.put(PreflightConstants.ANNOT_DICTIONARY_VALUE_SUBTYPE_TEXT, TextAnnotationValidator.class);
        this.validatorClasses.put(PreflightConstants.ANNOT_DICTIONARY_VALUE_SUBTYPE_LINK, LinkAnnotationValidator.class);
        this.validatorClasses.put(PreflightConstants.ANNOT_DICTIONARY_VALUE_SUBTYPE_FREETEXT, FreeTextAnnotationValidator.class);
        this.validatorClasses.put(PreflightConstants.ANNOT_DICTIONARY_VALUE_SUBTYPE_LINE, LineAnnotationValidator.class);

        this.validatorClasses.put(PreflightConstants.ANNOT_DICTIONARY_VALUE_SUBTYPE_SQUARE, SquareCircleAnnotationValidator.class);
        this.validatorClasses.put(PreflightConstants.ANNOT_DICTIONARY_VALUE_SUBTYPE_CIRCLE, SquareCircleAnnotationValidator.class);

        this.validatorClasses.put(PreflightConstants.ANNOT_DICTIONARY_VALUE_SUBTYPE_HIGHLIGHT, MarkupAnnotationValidator.class);
        this.validatorClasses.put(PreflightConstants.ANNOT_DICTIONARY_VALUE_SUBTYPE_UNDERLINE, MarkupAnnotationValidator.class);
        this.validatorClasses.put(PreflightConstants.ANNOT_DICTIONARY_VALUE_SUBTYPE_STRIKEOUT, MarkupAnnotationValidator.class);
        this.validatorClasses.put(PreflightConstants.ANNOT_DICTIONARY_VALUE_SUBTYPE_SQUILGGLY, MarkupAnnotationValidator.class);

        this.validatorClasses.put(PreflightConstants.ANNOT_DICTIONARY_VALUE_SUBTYPE_STAMP, RubberStampAnnotationValidator.class);
        this.validatorClasses.put(PreflightConstants.ANNOT_DICTIONARY_VALUE_SUBTYPE_INK, InkAnnotationValidator.class);
        this.validatorClasses.put(PreflightConstants.ANNOT_DICTIONARY_VALUE_SUBTYPE_POPUP, PopupAnnotationValidator.class);
        this.validatorClasses.put(PreflightConstants.ANNOT_DICTIONARY_VALUE_SUBTYPE_WIDGET, WidgetAnnotationValidator.class);
        this.validatorClasses.put(PreflightConstants.ANNOT_DICTIONARY_VALUE_SUBTYPE_PRINTERMARK, PrintMarkAnnotationValidator.class);
        this.validatorClasses.put(PreflightConstants.ANNOT_DICTIONARY_VALUE_SUBTYPE_TRAPNET, TrapNetAnnotationValidator.class);
    }

}
