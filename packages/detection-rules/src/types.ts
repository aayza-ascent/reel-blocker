export interface RuleSet {
  schemaVersion: number;
  instagramVersion: string;
  publishedAt: string;
  android: AndroidRules;
  ios: IosRules;
}

export interface AndroidRules {
  tabBarReelIds: string[];
  reelContainerIds: string[];
  reelContentDescriptions: string[];
  structuralSignatures: StructuralSignature[];
}

export interface StructuralSignature {
  parentClass: string;
  childPattern: string[];
  minChildCount: number;
  aspectRatioMin: number;
}

export interface IosRules {
  cssSelectors: string[];
  urlBlockPatterns: string[];
}
