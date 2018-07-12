const SamlStrategy = require('passport-saml').Strategy;
const bodyParser = require('body-parser');
const express = require('express');
const session = require('express-session');
const passport = require('passport');
const sha256 = require('js-sha256').sha256;

const cookieSession = require('cookie-session');
const cookieParser = require('cookie-parser');

// Create express instance
const app = express();

app.use(cookieParser());
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({extended: false}));
app.use(cookieSession({
  name: 'session',
  keys: [process.env.SESSION_KEY || 'https://app.use(bodyParser.json());.google.com/o/saml2/idp?idpid=C00nou4nu'],
  // Cookie Options
  maxAge: 2 * 24 * 60 * 60 * 1000 // 2 days
}));
app.use(passport.initialize());
app.use(passport.session());

passport.use(new SamlStrategy({
    protocol: 'https://',
    entryPoint: 'https://accounts.google.com/o/saml2/idp?idpid=C00nou4nu', // SSO URL (Step 2)
    issuer: 'https://hit.catalyst.munch.space/sp', // Entity ID (Step 4)
    path: '/auth/saml/callback', // ACS URL path (Step 4)

  }, function (profile, done) {
    done(null, {
      userId: sha256(profile.nameID),
      email: profile.email,
      name: profile.name
    });
  })
);

passport.serializeUser(function (user, done) {
  done(null, user);
});

passport.deserializeUser(function (user, done) {
  done(null, user);
});

app.get('/login', passport.authenticate('saml', {
  successRedirect: '/',
  failureRedirect: '/login'
}));

app.get('/logout', function (req, res) {
  req.logout();
  res.end('You have logged out of Munch HIT');
});

app.post('/auth/saml/callback', passport.authenticate('saml', {
  failureRedirect: '/error',
  failureFlash: true
}), function (req, res) {
  res.redirect('/');
});

// Securing every path except for /login
app.all('*', function (req, res, next) {
  if (req.isAuthenticated() || process.env.NODE_ENV !== 'production') {
    next();
  } else {
    res.redirect('/login');
  }
});

// Import API Routes
app.use(require('./routes/areas'));
app.use(require('./routes/brands'));
app.use(require('./routes/elastic'));
app.use(require('./routes/landmarks'));
app.use(require('./routes/tags'));

// Export the server middleware
module.exports = {
  path: '/',
  handler: app
};
