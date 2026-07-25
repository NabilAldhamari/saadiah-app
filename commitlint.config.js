module.exports = {
  extends: ['@commitlint/config-conventional'],
  rules: {
    'scope-enum': [
      2,
      'always',
      ['model', 'prayer', 'calendar', 'schedule', 'content', 'data', 'design', 'android', 'build', 'ci', 'docs', 'tools'],
    ],
    'subject-case': [2, 'always', 'lower-case'],
  },
};
