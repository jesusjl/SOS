/**
 * Copyright (C) 2012-2016 52°North Initiative for Geospatial Open Source
 * Software GmbH
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 as published
 * by the Free Software Foundation.
 *
 * If the program is linked with libraries which are licensed under one of
 * the following licenses, the combination of the program with the linked
 * library is not considered a "derivative work" of the program:
 *
 *     - Apache License, version 2.0
 *     - Apache Software License, version 1.0
 *     - GNU Lesser General Public License, version 3
 *     - Mozilla Public License, versions 1.0, 1.1 and 2.0
 *     - Common Development and Distribution License (CDDL), version 1.0
 *
 * Therefore the distribution of the program linked with libraries licensed
 * under the aforementioned licenses, is permitted by the copyright holders
 * if the distribution is compliant with both the GNU General Public
 * License version 2 and the aforementioned licenses.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 */
package org.n52.sos.ds.hibernate.util.observation;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.hibernate.Session;
import org.n52.sos.ds.hibernate.dao.observation.series.RelatedSeriesDAO;
import org.n52.sos.ds.hibernate.dao.observation.series.SeriesDAO;
import org.n52.sos.ds.hibernate.entities.observation.Observation;
import org.n52.sos.ds.hibernate.entities.observation.ereporting.AbstractEReportingObservation;
import org.n52.sos.ds.hibernate.entities.observation.ereporting.EReportingSeries;
import org.n52.sos.ds.hibernate.entities.observation.series.AbstractSeriesObservation;
import org.n52.sos.ds.hibernate.entities.observation.series.RelatedSeries;
import org.n52.sos.ds.hibernate.entities.observation.series.RelatedSeriesAdder;
import org.n52.sos.ds.hibernate.entities.observation.series.Series;
import org.n52.sos.exception.CodedException;
import org.n52.sos.ogc.om.OmObservation;

public class InspireObservationCreator implements AdditionalObservationCreator<Series> {

    private final static String NS_OMSO_30 = "http://inspire.ec.europa.eu/schemas/omso/3.0";

    private static final Set<AdditionalObservationCreatorKey> KEYS =
            AdditionalObservationCreatorRepository.encoderKeysForElements(NS_OMSO_30, 
                    AbstractSeriesObservation.class, 
                    AbstractEReportingObservation.class,
                    Series.class, 
                    EReportingSeries.class);

    @Override
    public Set<AdditionalObservationCreatorKey> getKeys() {
        return Collections.unmodifiableSet(KEYS);
    }

    @Override
    public OmObservation create(OmObservation omObservation, Series series) {
        SeriesDAO seriesDAO = new SeriesDAO();
        // TODO remove from PointObservation, profile, multipoint
        if (series.isSetIdentifier() && !omObservation.isSetIdentifier()) {
            omObservation.setIdentifier(seriesDAO.getIdentifier(series));
        }
        if (series.isSetName() && !omObservation.isSetName()) {
            omObservation.setName(seriesDAO.getName(series));
        }
        if (series.isSetDescription() && !omObservation.isSetDescription()) {
            omObservation.setDescription(series.getDescription());
        }
        return omObservation;
    }

    @Override
    public OmObservation create(OmObservation omObservation, Observation<?> observation) {
        return omObservation;
    }

    @Override
    public OmObservation add(OmObservation sosObservation, Observation<?> observation) {
        return sosObservation;
    }

    @Override
    public OmObservation create(OmObservation omObservation, Series series, Session session) throws CodedException {
        create(omObservation, series);
        // TODO remove from PointObservation, profile, multipoint
        addRelatedSeries(omObservation, new RelatedSeriesDAO().getRelatedSeries(series, session));
        return omObservation;
    }

    @Override
    public OmObservation create(OmObservation omObservation, Observation<?> observation, Session session) throws CodedException {
        create(omObservation, observation);
        if (observation instanceof AbstractSeriesObservation) {
            addRelatedSeries(omObservation, new RelatedSeriesDAO()
                    .getRelatedSeries(((AbstractSeriesObservation) observation).getSeries(), session));
        }
        return omObservation;
    }

    private void addRelatedSeries(OmObservation omObservation, List<RelatedSeries> relatedSeries) throws CodedException {
        new RelatedSeriesAdder(omObservation, relatedSeries).add();
    }

    @Override
    public OmObservation add(OmObservation omObservation, Observation<?> observation, Session session) throws CodedException {
        add(omObservation, observation);
        if (observation instanceof AbstractSeriesObservation) {
            addRelatedSeries(omObservation, new RelatedSeriesDAO()
                    .getRelatedSeries(((AbstractSeriesObservation) observation).getSeries(), session));
        }
        return omObservation;
    }

}
