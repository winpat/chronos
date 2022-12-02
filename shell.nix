{ pkgs ? import <nixpkgs> { } }:

pkgs.mkShell {
  buildInputs = [
    pkgs.clojure
    pkgs.docker-compose
    pkgs.postgresql
    pkgs.httpie
  ];
}
