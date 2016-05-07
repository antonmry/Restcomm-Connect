/*
 * TeleStax, Open Source Cloud Communications
 * Copyright 2011-2014, Telestax Inc and individual contributors
 * by the @authors tag.
 *
 * This program is free software: you can redistribute it and/or modify
 * under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation; either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 */
package org.mobicents.servlet.restcomm.amazonS3;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author anton@galiglobal.com (Anton R Yuste)
 */
public class AmazonS3Test {

    public AmazonS3Test() {
        super();
    }

    @Test @Ignore
    public void testSuccessScenario() {

        String accessKey = "YOUR ACCESS KEY";
        String securityKey = "YOUR SECURITY KEY";
        String bucketName = "BUCKET NAME";
        String folder = "";
        boolean reducedRedundancy = false;
        int daysToRetainPublicUrl = 180;
        boolean removeOriginalFile = false;
        String bucketRegion = "eu-west-1";
        String fileToUpload = "file:/tmp/test2.txt";


        S3AccessTool s3AccessTool = new S3AccessTool(
                accessKey,
                securityKey,
                bucketName,
                folder,
                reducedRedundancy,
                daysToRetainPublicUrl,
                removeOriginalFile,
                bucketRegion);

        assertTrue(s3AccessTool != null);

        URI publicS3Uri = s3AccessTool.uploadFile(fileToUpload);
        assertTrue(publicS3Uri != null);
    }
}
