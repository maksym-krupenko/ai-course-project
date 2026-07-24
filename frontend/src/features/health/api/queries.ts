import { gql } from "@apollo/client";

export interface HealthQueryResult {
  health: {
    status: string;
    databaseReachable: boolean;
    version: string;
  };
}

export const GET_HEALTH = gql`
  query GetHealth {
    health {
      status
      databaseReachable
      version
    }
  }
`;
